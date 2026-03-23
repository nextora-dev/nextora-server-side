package lk.iit.nextora.module.meeting.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.common.enums.MeetingType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.meeting.dto.request.*;
import lk.iit.nextora.module.meeting.dto.response.MeetingResponse;
import lk.iit.nextora.module.meeting.dto.response.MeetingStatisticsResponse;
import lk.iit.nextora.module.meeting.entity.Meeting;
import lk.iit.nextora.module.meeting.mapper.MeetingMapper;
import lk.iit.nextora.module.meeting.repository.MeetingRepository;
import lk.iit.nextora.module.meeting.service.MeetingService;
import lk.iit.nextora.infrastructure.notification.service.MeetingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of MeetingService for student-lecturer meeting requests.
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Student can request meetings with lecturers</li>
 *   <li>Lecturer can accept/reject/reschedule meetings</li>
 *   <li>Priority-based meeting requests</li>
 *   <li>Feedback/rating system after meetings</li>
 *   <li>Follow-up tracking</li>
 *   <li>Automatic reminders via scheduled tasks</li>
 *   <li>Comprehensive analytics and statistics</li>
 * </ul>
 *
 * @author Nextora Development Team
 * @version 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final StudentRepository studentRepository;
    private final AcademicStaffRepository academicStaffRepository;
    private final SecurityService securityService;
    private final MeetingMapper meetingMapper;
    private final MeetingNotificationService meetingNotificationService;

    // ==================== Student Operations ====================

    @Override
    @Transactional
    public MeetingResponse createMeetingRequest(CreateMeetingRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Student {} creating meeting request with lecturer {}", currentUserId, request.getLecturerId());

        // Get student (current user)
        Student student = studentRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", currentUserId));

        // Get lecturer
        AcademicStaff lecturer = academicStaffRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", request.getLecturerId()));

        // Check if lecturer is available for meetings
        if (!Boolean.TRUE.equals(lecturer.getAvailableForMeetings())) {
            throw new BadRequestException("This lecturer is not currently available for meetings");
        }

        // Create meeting entity
        Meeting meeting = meetingMapper.toEntity(request);
        meeting.setStudent(student);
        meeting.setLecturer(lecturer);
        meeting.setStatus(MeetingStatus.PENDING);

        // Set priority if provided, otherwise default
        if (request.getPriority() != null) {
            meeting.setPriority(request.getPriority());
        }

        meeting = meetingRepository.save(meeting);
        log.info("Meeting request {} created by student {} with priority {}",
                meeting.getId(), currentUserId, meeting.getPriority());

        // Send notification to lecturer about new meeting request
        meetingNotificationService.notifyNewMeetingRequest(
                meeting.getId(), lecturer.getId(),
                student.getFirstName() + " " + student.getLastName(),
                meeting.getSubject(),
                meeting.getPriorityDisplayName()
        );

        return meetingMapper.toResponse(meeting);
    }

    @Override
    public PagedResponse<MeetingResponse> getMyMeetingRequests(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findByStudentIdAndIsDeletedFalse(currentUserId, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> getMyMeetingRequestsByStatus(MeetingStatus status, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findByStudentIdAndStatusAndIsDeletedFalse(
                currentUserId, status, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> getMyUpcomingMeetings(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findUpcomingMeetingsForStudent(
                currentUserId, LocalDateTime.now(), pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> searchMyMeetings(String keyword, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.searchBySubjectForStudent(currentUserId, keyword, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    @Transactional
    public void cancelMeetingRequestByStudent(Long meetingId, String reason) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Student {} cancelling meeting request {}", currentUserId, meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify student owns this meeting
        if (!meeting.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only cancel your own meeting requests");
        }

        // Check if can be cancelled
        if (!meeting.canBeCancelled()) {
            throw new BadRequestException("This meeting cannot be cancelled in its current status");
        }

        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setCancellationReason(reason);
        meeting.setCancelledBy("STUDENT");
        meeting.setCancelledAt(LocalDateTime.now());

        meetingRepository.save(meeting);
        log.info("Meeting {} cancelled by student {}", meetingId, currentUserId);

        // Notify lecturer about cancellation
        meetingNotificationService.notifyMeetingCancelledByStudent(
                meetingId, meeting.getLecturer().getId(),
                meeting.getStudent().getFirstName() + " " + meeting.getStudent().getLastName(),
                reason
        );
    }

    @Override
    @Transactional
    public MeetingResponse submitFeedback(Long meetingId, SubmitFeedbackRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Student {} submitting feedback for meeting {}", currentUserId, meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify student owns this meeting
        if (!meeting.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only submit feedback for your own meetings");
        }

        // Check if feedback can be submitted
        if (!meeting.canSubmitFeedback()) {
            throw new BadRequestException("Feedback can only be submitted for completed meetings without existing feedback");
        }

        meeting.setStudentRating(request.getRating());
        meeting.setStudentFeedback(request.getFeedback());
        meeting.setFeedbackSubmittedAt(LocalDateTime.now());

        meeting = meetingRepository.save(meeting);
        log.info("Feedback submitted for meeting {} with rating {}", meetingId, request.getRating());

        return meetingMapper.toResponse(meeting);
    }

    // ==================== Lecturer Operations ====================

    @Override
    public PagedResponse<MeetingResponse> getPendingRequests(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findPendingRequestsForLecturer(currentUserId, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> getAllMyRequests(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findByLecturerIdAndIsDeletedFalse(currentUserId, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> getMyRequestsByStatus(MeetingStatus status, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findByLecturerIdAndStatusAndIsDeletedFalse(
                currentUserId, status, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> getMyUpcomingMeetingsAsLecturer(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findUpcomingMeetingsForLecturer(
                currentUserId, LocalDateTime.now(), pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public List<MeetingResponse> getCalendarMeetings(LocalDateTime startDate, LocalDateTime endDate) {
        Long currentUserId = securityService.getCurrentUserId();
        List<Meeting> meetings = meetingRepository.findLecturerCalendarMeetings(
                currentUserId, startDate, endDate);
        return meetingMapper.toResponseList(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> getHighPriorityRequests(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findHighPriorityRequestsForLecturer(currentUserId, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    @Transactional
    public MeetingResponse acceptMeetingRequest(Long meetingId, AcceptMeetingRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Lecturer {} accepting meeting request {}", currentUserId, meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify lecturer owns this meeting
        if (!meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only accept your own meeting requests");
        }

        // Check if still pending
        if (meeting.getStatus() != MeetingStatus.PENDING) {
            throw new BadRequestException("This meeting request has already been processed");
        }

        // Validate schedule
        validateMeetingSchedule(request.getScheduledStartTime(), request.getScheduledEndTime());

        // Check for conflicts
        if (meetingRepository.hasConflictingMeeting(currentUserId,
                request.getScheduledStartTime(), request.getScheduledEndTime())) {
            throw new BadRequestException("You have another meeting scheduled at this time. Please choose a different time slot.");
        }

        // Validate location info
        if (Boolean.TRUE.equals(request.getIsOnline())) {
            if (request.getMeetingLink() == null || request.getMeetingLink().isBlank()) {
                throw new BadRequestException("Meeting link is required for online meetings");
            }
        } else {
            if (request.getLocation() == null || request.getLocation().isBlank()) {
                throw new BadRequestException("Location is required for in-person meetings");
            }
        }

        // Update meeting
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setScheduledStartTime(request.getScheduledStartTime());
        meeting.setScheduledEndTime(request.getScheduledEndTime());
        meeting.setIsOnline(request.getIsOnline());
        meeting.setMeetingLink(request.getMeetingLink());
        meeting.setMeetingPlatform(request.getMeetingPlatform());
        meeting.setLocation(request.getLocation());
        meeting.setLecturerResponse(request.getResponseMessage());
        meeting.setRespondedAt(LocalDateTime.now());

        meeting = meetingRepository.save(meeting);
        log.info("Meeting {} accepted and scheduled for {}", meetingId, request.getScheduledStartTime());

        // Send notification to student with meeting details
        meetingNotificationService.notifyMeetingAccepted(
                meetingId, meeting.getStudent().getId(),
                meeting.getLecturer().getFirstName() + " " + meeting.getLecturer().getLastName(),
                request.getScheduledStartTime(), request.getLocation(),
                request.getMeetingLink(), request.getIsOnline()
        );

        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional
    public MeetingResponse rejectMeetingRequest(Long meetingId, RejectMeetingRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Lecturer {} rejecting meeting request {}", currentUserId, meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify lecturer owns this meeting
        if (!meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only reject your own meeting requests");
        }

        // Check if still pending
        if (meeting.getStatus() != MeetingStatus.PENDING) {
            throw new BadRequestException("This meeting request has already been processed");
        }

        meeting.setStatus(MeetingStatus.REJECTED);
        meeting.setLecturerResponse(request.getReason());
        meeting.setRespondedAt(LocalDateTime.now());

        meeting = meetingRepository.save(meeting);
        log.info("Meeting {} rejected with reason: {}", meetingId, request.getReason());

        // Send notification to student with rejection reason
        meetingNotificationService.notifyMeetingRejected(
                meetingId, meeting.getStudent().getId(),
                meeting.getLecturer().getFirstName() + " " + meeting.getLecturer().getLastName(),
                request.getReason()
        );

        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional
    public MeetingResponse rescheduleMeeting(Long meetingId, RescheduleMeetingRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Lecturer {} rescheduling meeting {}", currentUserId, meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify lecturer owns this meeting
        if (!meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only reschedule your own meetings");
        }

        // Check if can be rescheduled
        if (!meeting.canBeRescheduled()) {
            throw new BadRequestException("This meeting cannot be rescheduled in its current status");
        }

        // Validate new schedule
        validateMeetingSchedule(request.getScheduledStartTime(), request.getScheduledEndTime());

        // Check for conflicts (excluding this meeting)
        if (meetingRepository.hasConflictingMeetingExcluding(currentUserId, meetingId,
                request.getScheduledStartTime(), request.getScheduledEndTime())) {
            throw new BadRequestException("You have another meeting scheduled at this time");
        }

        // Update meeting
        LocalDateTime oldStartTime = meeting.getScheduledStartTime();
        meeting.setScheduledStartTime(request.getScheduledStartTime());
        meeting.setScheduledEndTime(request.getScheduledEndTime());
        meeting.setStatus(MeetingStatus.RESCHEDULED);

        if (request.getIsOnline() != null) {
            meeting.setIsOnline(request.getIsOnline());
        }
        if (request.getMeetingLink() != null) {
            meeting.setMeetingLink(request.getMeetingLink());
        }
        if (request.getMeetingPlatform() != null) {
            meeting.setMeetingPlatform(request.getMeetingPlatform());
        }
        if (request.getLocation() != null) {
            meeting.setLocation(request.getLocation());
        }
        if (request.getReason() != null) {
            meeting.setLecturerResponse("Rescheduled from " + oldStartTime + ": " + request.getReason());
        }

        // Reset notification flags so student gets notified of new time
        meeting.setStudentNotified(false);
        meeting.setReminderSent(false);
        meeting.setFinalReminderSent(false);

        meeting = meetingRepository.save(meeting);
        log.info("Meeting {} rescheduled from {} to {}", meetingId, oldStartTime, request.getScheduledStartTime());

        // Send notification to student about reschedule
        meetingNotificationService.notifyMeetingRescheduled(
                meetingId, meeting.getStudent().getId(),
                meeting.getLecturer().getFirstName() + " " + meeting.getLecturer().getLastName(),
                request.getScheduledStartTime()
        );

        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional
    public void cancelMeetingByLecturer(Long meetingId, String reason) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Lecturer {} cancelling meeting {}", currentUserId, meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify lecturer owns this meeting
        if (!meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only cancel your own meetings");
        }

        // Check if can be cancelled
        if (!meeting.canBeCancelled()) {
            throw new BadRequestException("This meeting cannot be cancelled in its current status");
        }

        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setCancellationReason(reason);
        meeting.setCancelledBy("LECTURER");
        meeting.setCancelledAt(LocalDateTime.now());

        meetingRepository.save(meeting);
        log.info("Meeting {} cancelled by lecturer with reason: {}", meetingId, reason);

        // Notify student about cancellation
        meetingNotificationService.notifyMeetingCancelledByLecturer(
                meetingId, meeting.getStudent().getId(),
                meeting.getLecturer().getFirstName() + " " + meeting.getLecturer().getLastName(),
                reason
        );
    }

    @Override
    @Transactional
    public MeetingResponse addMeetingNotes(Long meetingId, String notes, boolean followUpRequired, String followUpNotes) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Lecturer {} adding notes to meeting {}", currentUserId, meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify lecturer owns this meeting
        if (!meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only add notes to your own meetings");
        }

        meeting.setMeetingNotes(notes);
        meeting.setFollowUpRequired(followUpRequired);
        meeting.setFollowUpNotes(followUpNotes);

        meeting = meetingRepository.save(meeting);
        log.info("Notes added to meeting {}. Follow-up required: {}", meetingId, followUpRequired);

        return meetingMapper.toResponse(meeting);
    }

    @Override
    public PagedResponse<MeetingResponse> searchMyMeetingsAsLecturer(String keyword, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.searchForLecturer(currentUserId, keyword, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public MeetingStatisticsResponse getMyStatistics() {
        Long currentUserId = securityService.getCurrentUserId();
        return buildStatisticsForLecturer(currentUserId);
    }

    @Override
    public long getPendingRequestsCount() {
        Long currentUserId = securityService.getCurrentUserId();
        return meetingRepository.countPendingRequestsForLecturer(currentUserId);
    }

    @Override
    public PagedResponse<MeetingResponse> getMeetingsRequiringFollowUp(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Meeting> meetings = meetingRepository.findMeetingsRequiringFollowUp(currentUserId, pageable);
        return toPagedResponse(meetings);
    }

    // ==================== Common Operations ====================

    @Override
    public MeetingResponse getMeetingById(Long meetingId) {
        Meeting meeting = findMeetingById(meetingId);

        // Verify access (student or lecturer of this meeting, or admin)
        Long currentUserId = securityService.getCurrentUserId();
        if (!meeting.getStudent().getId().equals(currentUserId) &&
            !meeting.getLecturer().getId().equals(currentUserId)) {
            // TODO: Check if user is admin
            throw new UnauthorizedException("You do not have access to this meeting");
        }

        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional
    public MeetingResponse startMeeting(Long meetingId) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Starting meeting {}", meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify lecturer owns this meeting
        if (!meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the lecturer can start the meeting");
        }

        if (meeting.getStatus() != MeetingStatus.SCHEDULED &&
            meeting.getStatus() != MeetingStatus.RESCHEDULED) {
            throw new BadRequestException("Only scheduled meetings can be started");
        }

        meeting.setStatus(MeetingStatus.IN_PROGRESS);
        meeting.setActualStartTime(LocalDateTime.now());
        meeting = meetingRepository.save(meeting);

        log.info("Meeting {} started at {}", meetingId, meeting.getActualStartTime());

        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional
    public MeetingResponse completeMeeting(Long meetingId) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Completing meeting {}", meetingId);

        Meeting meeting = findMeetingById(meetingId);

        // Verify lecturer owns this meeting
        if (!meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the lecturer can complete the meeting");
        }

        if (meeting.getStatus() != MeetingStatus.IN_PROGRESS) {
            throw new BadRequestException("Only in-progress meetings can be completed");
        }

        meeting.setStatus(MeetingStatus.COMPLETED);
        meeting.setActualEndTime(LocalDateTime.now());
        meeting = meetingRepository.save(meeting);

        log.info("Meeting {} completed. Duration: {} minutes",
                meetingId, meeting.getActualDurationMinutes());

        // Send notification to student asking for feedback
        meetingNotificationService.notifyMeetingCompleted(
                meetingId, meeting.getStudent().getId(),
                meeting.getLecturer().getFirstName() + " " + meeting.getLecturer().getLastName()
        );

        return meetingMapper.toResponse(meeting);
    }

    // ==================== Admin Operations ====================

    @Override
    public PagedResponse<MeetingResponse> getAllMeetings(Pageable pageable) {
        log.debug("Admin fetching all meetings");
        Page<Meeting> meetings = meetingRepository.findByIsDeletedFalse(pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public PagedResponse<MeetingResponse> getAllMeetingsByStatus(MeetingStatus status, Pageable pageable) {
        log.debug("Admin fetching meetings by status: {}", status);
        Page<Meeting> meetings = meetingRepository.findByStatusAndIsDeletedFalse(status, pageable);
        return toPagedResponse(meetings);
    }

    @Override
    public MeetingStatisticsResponse getPlatformStatistics() {
        log.debug("Building platform-wide statistics");
        return buildPlatformStatistics();
    }

    @Override
    public MeetingStatisticsResponse getLecturerStatistics(Long lecturerId) {
        log.debug("Building statistics for lecturer {}", lecturerId);
        return buildStatisticsForLecturer(lecturerId);
    }

    @Override
    @Transactional
    public void adminCancelMeeting(Long meetingId, String reason) {
        log.info("Admin cancelling meeting {} with reason: {}", meetingId, reason);

        Meeting meeting = findMeetingById(meetingId);

        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setCancellationReason("Admin: " + reason);
        meeting.setCancelledBy("ADMIN");
        meeting.setCancelledAt(LocalDateTime.now());

        meetingRepository.save(meeting);

        // Notify both student and lecturer
        meetingNotificationService.notifyAdminCancelledMeeting(
                meetingId, meeting.getStudent().getId(),
                meeting.getLecturer().getId(), reason
        );
    }

    @Override
    @Transactional
    public void permanentlyDeleteMeeting(Long meetingId) {
        log.warn("Admin permanently deleting meeting {}", meetingId);

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));

        meetingRepository.delete(meeting);
    }

    // ==================== Attachment ====================

    @Override
    @Transactional
    public void updateAttachment(Long meetingId, String attachmentUrl, String attachmentName) {
        Long currentUserId = securityService.getCurrentUserId();
        Meeting meeting = findMeetingById(meetingId);

        if (!meeting.getStudent().getId().equals(currentUserId) &&
            !meeting.getLecturer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have access to this meeting");
        }

        meeting.setAttachmentUrl(attachmentUrl);
        meeting.setAttachmentName(attachmentName);
        meetingRepository.save(meeting);
        log.info("Attachment updated for meeting {}: {}", meetingId, attachmentName);
    }

    // ==================== Scheduled Tasks ====================

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Every minute
    public void processScheduledMeetings() {
        LocalDateTime now = LocalDateTime.now();
        List<Meeting> meetingsToStart = meetingRepository.findMeetingsToStart(now);

        for (Meeting meeting : meetingsToStart) {
            meeting.setStatus(MeetingStatus.IN_PROGRESS);
            meeting.setActualStartTime(now);
            meetingRepository.save(meeting);
            log.info("Auto-started meeting {} at {}", meeting.getId(), now);
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Every minute
    public void processCompletedMeetings() {
        LocalDateTime now = LocalDateTime.now();
        List<Meeting> meetingsToComplete = meetingRepository.findMeetingsToComplete(now);

        for (Meeting meeting : meetingsToComplete) {
            meeting.setStatus(MeetingStatus.COMPLETED);
            meeting.setActualEndTime(now);
            meetingRepository.save(meeting);
            log.info("Auto-completed meeting {} at {}", meeting.getId(), now);
            // Send feedback request to student
            meetingNotificationService.notifyMeetingCompleted(
                    meeting.getId(), meeting.getStudent().getId(),
                    meeting.getLecturer().getFirstName() + " " + meeting.getLecturer().getLastName()
            );
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000) // Every hour
    public void sendMeetingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(24);

        List<Meeting> meetingsNeedingReminder = meetingRepository.findMeetingsNeedingReminder(now, reminderTime);

        for (Meeting meeting : meetingsNeedingReminder) {
            meetingNotificationService.notifyMeetingReminder(
                    meeting.getId(), meeting.getStudent().getId(),
                    meeting.getLecturer().getId(), meeting.getSubject(),
                    meeting.getScheduledStartTime(), 1440
            );
            meeting.setReminderSent(true);
            meetingRepository.save(meeting);
            log.info("Sent 24-hour reminder for meeting {}", meeting.getId());
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 900000) // Every 15 minutes
    public void sendFinalMeetingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finalReminderTime = now.plusHours(1);

        List<Meeting> meetingsNeedingFinalReminder = meetingRepository.findMeetingsNeedingFinalReminder(now, finalReminderTime);

        for (Meeting meeting : meetingsNeedingFinalReminder) {
            meetingNotificationService.notifyMeetingReminder(
                    meeting.getId(), meeting.getStudent().getId(),
                    meeting.getLecturer().getId(), meeting.getSubject(),
                    meeting.getScheduledStartTime(), 60
            );
            meeting.setFinalReminderSent(true);
            meetingRepository.save(meeting);
            log.info("Sent 1-hour reminder for meeting {}", meeting.getId());
        }
    }

    // ==================== Private Helper Methods ====================

    private Meeting findMeetingById(Long meetingId) {
        return meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
    }

    private void validateMeetingSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time are required");
        }
        if (startTime.isAfter(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }
        // Minimum 15 minutes, maximum 3 hours
        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 15) {
            throw new BadRequestException("Meeting duration must be at least 15 minutes");
        }
        if (durationMinutes > 180) {
            throw new BadRequestException("Meeting duration cannot exceed 3 hours");
        }
    }

    private MeetingStatisticsResponse buildStatisticsForLecturer(Long lecturerId) {
        // Get basic counts
        long total = meetingRepository.countByLecturerIdAndIsDeletedFalse(lecturerId);
        long pending = meetingRepository.countByLecturerIdAndStatusAndIsDeletedFalse(lecturerId, MeetingStatus.PENDING);
        long scheduled = meetingRepository.countByLecturerIdAndStatusAndIsDeletedFalse(lecturerId, MeetingStatus.SCHEDULED);
        long completed = meetingRepository.countByLecturerIdAndStatusAndIsDeletedFalse(lecturerId, MeetingStatus.COMPLETED);
        long rejected = meetingRepository.countByLecturerIdAndStatusAndIsDeletedFalse(lecturerId, MeetingStatus.REJECTED);
        long cancelled = meetingRepository.countByLecturerIdAndStatusAndIsDeletedFalse(lecturerId, MeetingStatus.CANCELLED);

        // Calculate rates
        double acceptanceRate = total > 0 ? (double)(scheduled + completed) / total * 100 : 0;
        double completionRate = scheduled > 0 ? (double) completed / scheduled * 100 : 0;
        double cancellationRate = total > 0 ? (double) cancelled / total * 100 : 0;

        return MeetingStatisticsResponse.builder()
                .totalRequests(total)
                .pendingRequests(pending)
                .scheduledMeetings(scheduled)
                .completedMeetings(completed)
                .rejectedRequests(rejected)
                .cancelledMeetings(cancelled)
                .academicGuidanceMeetings(meetingRepository.countByLecturerIdAndMeetingTypeAndIsDeletedFalse(lecturerId, MeetingType.ACADEMIC_GUIDANCE))
                .projectDiscussionMeetings(meetingRepository.countByLecturerIdAndMeetingTypeAndIsDeletedFalse(lecturerId, MeetingType.PROJECT_DISCUSSION))
                .careerCounselingMeetings(meetingRepository.countByLecturerIdAndMeetingTypeAndIsDeletedFalse(lecturerId, MeetingType.CAREER_COUNSELING))
                .personalConsultationMeetings(meetingRepository.countByLecturerIdAndMeetingTypeAndIsDeletedFalse(lecturerId, MeetingType.PERSONAL_CONSULTATION))
                .researchDiscussionMeetings(meetingRepository.countByLecturerIdAndMeetingTypeAndIsDeletedFalse(lecturerId, MeetingType.RESEARCH_DISCUSSION))
                .otherMeetings(meetingRepository.countByLecturerIdAndMeetingTypeAndIsDeletedFalse(lecturerId, MeetingType.OTHER))
                .onlineMeetings(meetingRepository.countByLecturerIdAndIsOnline(lecturerId, true))
                .inPersonMeetings(meetingRepository.countByLecturerIdAndIsOnline(lecturerId, false))
                .acceptanceRate(Math.round(acceptanceRate * 100.0) / 100.0)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                .meetingsRequiringFollowUp(meetingRepository.countMeetingsRequiringFollowUp(lecturerId))
                .build();
    }

    private MeetingStatisticsResponse buildPlatformStatistics() {
        long total = meetingRepository.count();
        long pending = meetingRepository.countByStatusAndIsDeletedFalse(MeetingStatus.PENDING);
        long scheduled = meetingRepository.countByStatusAndIsDeletedFalse(MeetingStatus.SCHEDULED);
        long completed = meetingRepository.countByStatusAndIsDeletedFalse(MeetingStatus.COMPLETED);
        long rejected = meetingRepository.countByStatusAndIsDeletedFalse(MeetingStatus.REJECTED);
        long cancelled = meetingRepository.countByStatusAndIsDeletedFalse(MeetingStatus.CANCELLED);

        double acceptanceRate = total > 0 ? (double)(scheduled + completed) / total * 100 : 0;
        double completionRate = scheduled > 0 ? (double) completed / scheduled * 100 : 0;
        double cancellationRate = total > 0 ? (double) cancelled / total * 100 : 0;

        return MeetingStatisticsResponse.builder()
                .totalRequests(total)
                .pendingRequests(pending)
                .scheduledMeetings(scheduled)
                .completedMeetings(completed)
                .rejectedRequests(rejected)
                .cancelledMeetings(cancelled)
                .academicGuidanceMeetings(meetingRepository.countByMeetingTypeAndIsDeletedFalse(MeetingType.ACADEMIC_GUIDANCE))
                .projectDiscussionMeetings(meetingRepository.countByMeetingTypeAndIsDeletedFalse(MeetingType.PROJECT_DISCUSSION))
                .careerCounselingMeetings(meetingRepository.countByMeetingTypeAndIsDeletedFalse(MeetingType.CAREER_COUNSELING))
                .personalConsultationMeetings(meetingRepository.countByMeetingTypeAndIsDeletedFalse(MeetingType.PERSONAL_CONSULTATION))
                .researchDiscussionMeetings(meetingRepository.countByMeetingTypeAndIsDeletedFalse(MeetingType.RESEARCH_DISCUSSION))
                .otherMeetings(meetingRepository.countByMeetingTypeAndIsDeletedFalse(MeetingType.OTHER))
                .acceptanceRate(Math.round(acceptanceRate * 100.0) / 100.0)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                .build();
    }

    private PagedResponse<MeetingResponse> toPagedResponse(Page<Meeting> page) {
        List<MeetingResponse> content = page.getContent().stream()
                .map(meetingMapper::toResponse)
                .toList();

        return PagedResponse.<MeetingResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
