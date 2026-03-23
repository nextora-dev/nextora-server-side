package lk.iit.nextora.module.meeting.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.module.meeting.dto.request.*;
import lk.iit.nextora.module.meeting.dto.response.MeetingResponse;
import lk.iit.nextora.module.meeting.dto.response.MeetingStatisticsResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Meeting operations.
 *
 * <h3>MEETING FLOW:</h3>
 * <ol>
 *   <li>Student requests meeting with a lecturer → {@link #createMeetingRequest(CreateMeetingRequest)}</li>
 *   <li>Lecturer views pending requests → {@link #getPendingRequests(Pageable)}</li>
 *   <li>Lecturer accepts with time/location → {@link #acceptMeetingRequest(Long, AcceptMeetingRequest)}<br>
 *       OR Lecturer rejects with reason → {@link #rejectMeetingRequest(Long, RejectMeetingRequest)}</li>
 *   <li>Student receives notification</li>
 *   <li>Lecturer's calendar is updated</li>
 *   <li>Meeting can be rescheduled → {@link #rescheduleMeeting(Long, RescheduleMeetingRequest)}</li>
 *   <li>Meeting can be cancelled → {@link #cancelMeetingRequestByStudent(Long, String)} or {@link #cancelMeetingByLecturer(Long, String)}</li>
 *   <li>After meeting, notes can be added → {@link #addMeetingNotes(Long, String, boolean, String)}</li>
 *   <li>Student can submit feedback → {@link #submitFeedback(Long, SubmitFeedbackRequest)}</li>
 * </ol>
 *
 * @author Nextora Development Team
 * @version 2.0
 */
public interface MeetingService {

    // ==================== Student Operations ====================

    /**
     * Create a new meeting request.
     * Called by a STUDENT to request a meeting with a lecturer.
     *
     * @param request Meeting request details including lecturer ID, subject, type, and preferred time
     * @return Created meeting response
     * @throws lk.iit.nextora.common.exception.custom.ResourceNotFoundException if lecturer not found
     * @throws lk.iit.nextora.common.exception.custom.BadRequestException if lecturer is not available for meetings
     */
    MeetingResponse createMeetingRequest(CreateMeetingRequest request);

    /**
     * Get all meeting requests made by the current student.
     *
     * @param pageable Pagination info
     * @return Paginated list of meetings
     */
    PagedResponse<MeetingResponse> getMyMeetingRequests(Pageable pageable);

    /**
     * Get meeting requests filtered by status for the current student.
     *
     * @param status Meeting status filter
     * @param pageable Pagination info
     * @return Paginated list of meetings
     */
    PagedResponse<MeetingResponse> getMyMeetingRequestsByStatus(MeetingStatus status, Pageable pageable);

    /**
     * Get upcoming scheduled meetings for the current student.
     *
     * @param pageable Pagination info
     * @return Paginated list of upcoming meetings
     */
    PagedResponse<MeetingResponse> getMyUpcomingMeetings(Pageable pageable);

    /**
     * Search student's meetings by keyword (searches subject, description, lecturer name).
     *
     * @param keyword Search keyword
     * @param pageable Pagination info
     * @return Paginated list of matching meetings
     */
    PagedResponse<MeetingResponse> searchMyMeetings(String keyword, Pageable pageable);

    /**
     * Cancel a meeting request (by student).
     * Can only cancel pending or scheduled meetings.
     *
     * @param meetingId Meeting ID
     * @param reason Cancellation reason
     * @throws lk.iit.nextora.common.exception.custom.UnauthorizedException if not the student's meeting
     * @throws lk.iit.nextora.common.exception.custom.BadRequestException if meeting cannot be cancelled
     */
    void cancelMeetingRequestByStudent(Long meetingId, String reason);

    /**
     * Submit feedback after a completed meeting.
     *
     * @param meetingId Meeting ID
     * @param request Feedback with rating and optional comment
     * @return Updated meeting response
     * @throws lk.iit.nextora.common.exception.custom.BadRequestException if meeting is not completed or feedback already submitted
     */
    MeetingResponse submitFeedback(Long meetingId, SubmitFeedbackRequest request);

    // ==================== Lecturer Operations ====================

    /**
     * Get all pending meeting requests for the current lecturer.
     * These are requests waiting for the lecturer's response.
     *
     * @param pageable Pagination info
     * @return Paginated list of pending meeting requests
     */
    PagedResponse<MeetingResponse> getPendingRequests(Pageable pageable);

    /**
     * Get all meeting requests for the current lecturer (all statuses).
     *
     * @param pageable Pagination info
     * @return Paginated list of all meeting requests
     */
    PagedResponse<MeetingResponse> getAllMyRequests(Pageable pageable);

    /**
     * Get meeting requests filtered by status for the current lecturer.
     *
     * @param status Meeting status filter
     * @param pageable Pagination info
     * @return Paginated list of meetings
     */
    PagedResponse<MeetingResponse> getMyRequestsByStatus(MeetingStatus status, Pageable pageable);

    /**
     * Get upcoming scheduled meetings for the current lecturer.
     *
     * @param pageable Pagination info
     * @return Paginated list of upcoming meetings
     */
    PagedResponse<MeetingResponse> getMyUpcomingMeetingsAsLecturer(Pageable pageable);

    /**
     * Get meetings for lecturer's calendar within a date range.
     * Returns all scheduled/completed meetings for calendar display.
     *
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of meetings within the date range
     */
    List<MeetingResponse> getCalendarMeetings(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get high priority meeting requests for the current lecturer.
     * Priority 3 (High) and 4 (Urgent).
     *
     * @param pageable Pagination info
     * @return Paginated list of high priority meetings
     */
    PagedResponse<MeetingResponse> getHighPriorityRequests(Pageable pageable);

    /**
     * Accept a meeting request and schedule it.
     * This sets the time, location, and triggers notifications.
     *
     * @param meetingId Meeting ID
     * @param request Accept request with schedule details
     * @return Updated meeting response
     * @throws lk.iit.nextora.common.exception.custom.UnauthorizedException if not the lecturer's meeting
     * @throws lk.iit.nextora.common.exception.custom.BadRequestException if meeting already processed or conflicts exist
     */
    MeetingResponse acceptMeetingRequest(Long meetingId, AcceptMeetingRequest request);

    /**
     * Reject a meeting request.
     * The student will be notified with the reason.
     *
     * @param meetingId Meeting ID
     * @param request Reject request with reason
     * @return Updated meeting response
     */
    MeetingResponse rejectMeetingRequest(Long meetingId, RejectMeetingRequest request);

    /**
     * Reschedule an existing meeting.
     * The student will be notified of the new time.
     *
     * @param meetingId Meeting ID
     * @param request Reschedule request with new time
     * @return Updated meeting response
     */
    MeetingResponse rescheduleMeeting(Long meetingId, RescheduleMeetingRequest request);

    /**
     * Cancel a meeting (by lecturer).
     *
     * @param meetingId Meeting ID
     * @param reason Cancellation reason
     */
    void cancelMeetingByLecturer(Long meetingId, String reason);

    /**
     * Add notes to a meeting and optionally mark for follow-up.
     *
     * @param meetingId Meeting ID
     * @param notes Meeting notes/summary
     * @param followUpRequired Whether follow-up is needed
     * @param followUpNotes Follow-up action items
     * @return Updated meeting response
     */
    MeetingResponse addMeetingNotes(Long meetingId, String notes, boolean followUpRequired, String followUpNotes);

    /**
     * Search lecturer's meetings by keyword (searches subject, student name).
     *
     * @param keyword Search keyword
     * @param pageable Pagination info
     * @return Paginated list of matching meetings
     */
    PagedResponse<MeetingResponse> searchMyMeetingsAsLecturer(String keyword, Pageable pageable);

    /**
     * Get statistics for the current lecturer.
     *
     * @return Comprehensive meeting statistics
     */
    MeetingStatisticsResponse getMyStatistics();

    /**
     * Get count of pending requests for the current lecturer.
     * Useful for showing notification badge.
     *
     * @return Number of pending requests
     */
    long getPendingRequestsCount();

    /**
     * Get meetings requiring follow-up for the current lecturer.
     *
     * @param pageable Pagination info
     * @return Paginated list of meetings needing follow-up
     */
    PagedResponse<MeetingResponse> getMeetingsRequiringFollowUp(Pageable pageable);

    // ==================== Common Operations ====================

    /**
     * Get meeting by ID.
     * Validates that the current user has access (is student or lecturer of the meeting).
     *
     * @param meetingId Meeting ID
     * @return Meeting response
     * @throws lk.iit.nextora.common.exception.custom.ResourceNotFoundException if meeting not found
     * @throws lk.iit.nextora.common.exception.custom.UnauthorizedException if user doesn't have access
     */
    MeetingResponse getMeetingById(Long meetingId);

    /**
     * Start a meeting (change status to IN_PROGRESS).
     * Records the actual start time.
     *
     * @param meetingId Meeting ID
     * @return Updated meeting response
     */
    MeetingResponse startMeeting(Long meetingId);

    /**
     * End a meeting (change status to COMPLETED).
     * Records the actual end time.
     *
     * @param meetingId Meeting ID
     * @return Updated meeting response
     */
    MeetingResponse completeMeeting(Long meetingId);

    // ==================== Lecturer List (for students) ====================

    /**
     * Get list of lecturers available for meetings.
     * Students can use this to select who to request a meeting with.
     *
     * @param department Optional department filter
     * @param pageable Pagination info
     * @return Paginated list of available lecturers
     */
    // PagedResponse<LecturerResponse> getAvailableLecturers(String department, Pageable pageable);

    // ==================== Admin Operations ====================

    /**
     * Get all meetings (admin view).
     *
     * @param pageable Pagination info
     * @return Paginated list of all meetings
     */
    PagedResponse<MeetingResponse> getAllMeetings(Pageable pageable);

    /**
     * Get all meetings filtered by status (admin view).
     *
     * @param status Meeting status filter
     * @param pageable Pagination info
     * @return Paginated list of meetings
     */
    PagedResponse<MeetingResponse> getAllMeetingsByStatus(MeetingStatus status, Pageable pageable);

    /**
     * Get platform-wide meeting statistics.
     *
     * @return Platform statistics
     */
    MeetingStatisticsResponse getPlatformStatistics();

    /**
     * Get statistics for a specific lecturer.
     *
     * @param lecturerId Lecturer ID
     * @return Lecturer statistics
     */
    MeetingStatisticsResponse getLecturerStatistics(Long lecturerId);

    /**
     * Force cancel any meeting (admin).
     *
     * @param meetingId Meeting ID
     * @param reason Cancellation reason
     */
    void adminCancelMeeting(Long meetingId, String reason);

    /**
     * Permanently delete a meeting (admin).
     * This is a hard delete, use with caution.
     *
     * @param meetingId Meeting ID
     */
    void permanentlyDeleteMeeting(Long meetingId);

    // ==================== Attachment ====================

    /**
     * Update the attachment for a meeting.
     *
     * @param meetingId Meeting ID
     * @param attachmentUrl S3 URL of the attachment
     * @param attachmentName Original file name
     */
    void updateAttachment(Long meetingId, String attachmentUrl, String attachmentName);

    // ==================== Scheduled Tasks ====================

    /**
     * Process meetings that should be auto-started.
     * Called by scheduler when meeting start time is reached.
     */
    void processScheduledMeetings();

    /**
     * Process meetings that should be auto-completed.
     * Called by scheduler when meeting end time is reached.
     */
    void processCompletedMeetings();

    /**
     * Send meeting reminders (24 hours before).
     * Called by scheduler.
     */
    void sendMeetingReminders();

    /**
     * Send final meeting reminders (1 hour before).
     * Called by scheduler.
     */
    void sendFinalMeetingReminders();
}
