package lk.iit.nextora.module.meeting.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.meeting.dto.request.CreateMeetingRequest;
import lk.iit.nextora.module.meeting.dto.response.MeetingResponse;
import lk.iit.nextora.module.meeting.entity.Meeting;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Meeting module entities and DTOs.
 *
 * <p>Handles conversions between:</p>
 * <ul>
 *   <li>Meeting entity ↔ MeetingResponse DTO</li>
 *   <li>CreateMeetingRequest → Meeting entity</li>
 * </ul>
 *
 * <p>Uses the common MapperConfiguration for consistent mapping behavior.</p>
 *
 * @author Nextora Development Team
 * @version 2.0
 */
@Mapper(config = MapperConfiguration.class)
public interface MeetingMapper {

    // ==================== Meeting Entity to Response ====================

    /**
     * Convert Meeting entity to MeetingResponse DTO.
     * Maps student, lecturer information, and computed fields.
     */
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(getFullName(meeting.getStudent().getFirstName(), meeting.getStudent().getLastName()))")
    @Mapping(target = "studentEmail", source = "student.email")
    @Mapping(target = "studentBatch", source = "student.batch")
    @Mapping(target = "studentProgram", source = "student.program")
    @Mapping(target = "lecturerId", source = "lecturer.id")
    @Mapping(target = "lecturerName", expression = "java(getFullName(meeting.getLecturer().getFirstName(), meeting.getLecturer().getLastName()))")
    @Mapping(target = "lecturerEmail", source = "lecturer.email")
    @Mapping(target = "lecturerDepartment", source = "lecturer.department")
    @Mapping(target = "lecturerDesignation", source = "lecturer.designation")
    @Mapping(target = "lecturerOfficeLocation", source = "lecturer.officeLocation")
    @Mapping(target = "lecturerProfileImageUrl", source = "lecturer.profileImageUrl")
    @Mapping(target = "meetingTypeDisplayName", expression = "java(meeting.getMeetingType().getDisplayName())")
    @Mapping(target = "statusDisplayName", expression = "java(meeting.getStatus().getDisplayName())")
    @Mapping(target = "priorityDisplayName", expression = "java(meeting.getPriorityDisplayName())")
    @Mapping(target = "durationMinutes", expression = "java(meeting.getDurationMinutes())")
    @Mapping(target = "actualDurationMinutes", expression = "java(meeting.getActualDurationMinutes())")
    @Mapping(target = "responseTimeHours", expression = "java(meeting.getResponseTimeHours())")
    @Mapping(target = "canJoin", expression = "java(meeting.isJoinable())")
    @Mapping(target = "canCancel", expression = "java(meeting.canBeCancelled())")
    @Mapping(target = "canReschedule", expression = "java(meeting.canBeRescheduled())")
    @Mapping(target = "canSubmitFeedback", expression = "java(meeting.canSubmitFeedback())")
    @Mapping(target = "isHighPriority", expression = "java(meeting.isHighPriority())")
    @Mapping(target = "isRecurring", expression = "java(meeting.isRecurring())")
    MeetingResponse toResponse(Meeting meeting);

    /**
     * Convert list of Meeting entities to list of MeetingResponse DTOs.
     */
    List<MeetingResponse> toResponseList(List<Meeting> meetings);

    // ==================== Request to Entity ====================

    /**
     * Convert CreateMeetingRequest to Meeting entity.
     * Note: student, lecturer, and audit fields are set in the service layer.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "lecturer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "scheduledStartTime", ignore = true)
    @Mapping(target = "scheduledEndTime", ignore = true)
    @Mapping(target = "isOnline", ignore = true)
    @Mapping(target = "meetingLink", ignore = true)
    @Mapping(target = "meetingPlatform", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "lecturerResponse", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "meetingNotes", ignore = true)
    @Mapping(target = "actualStartTime", ignore = true)
    @Mapping(target = "actualEndTime", ignore = true)
    @Mapping(target = "followUpRequired", ignore = true)
    @Mapping(target = "followUpNotes", ignore = true)
    @Mapping(target = "studentRating", ignore = true)
    @Mapping(target = "studentFeedback", ignore = true)
    @Mapping(target = "feedbackSubmittedAt", ignore = true)
    @Mapping(target = "parentMeetingId", ignore = true)
    @Mapping(target = "recurrencePattern", ignore = true)
    @Mapping(target = "recurrenceEndDate", ignore = true)
    @Mapping(target = "calendarEventId", ignore = true)
    @Mapping(target = "calendarSynced", ignore = true)
    @Mapping(target = "studentNotified", ignore = true)
    @Mapping(target = "reminderSent", ignore = true)
    @Mapping(target = "finalReminderSent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Meeting toEntity(CreateMeetingRequest request);

    // ==================== Helper Methods ====================

    /**
     * Helper method to construct full name from first and last name
     */
    default String getFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (firstName != null) {
            sb.append(firstName);
        }
        if (lastName != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(lastName);
        }
        return sb.toString();
    }
}
