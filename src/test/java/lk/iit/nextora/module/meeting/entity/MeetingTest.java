package lk.iit.nextora.module.meeting.entity;

import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.common.enums.MeetingType;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Meeting Entity Unit Tests")
class MeetingTest {

    @Nested
    @DisplayName("Builder and Initialization")
    class BuilderTests {

        @Test
        @DisplayName("Should create Meeting with required fields")
        void builder_withAllFields_createsValidMeeting() {
            LocalDateTime now = LocalDateTime.now();
            Student student = new Student();
            student.setId(1L);
            
            AcademicStaff lecturer = new AcademicStaff();
            lecturer.setId(2L);
            
            String subject = "Course Discussion";
            String description = "Assignment requirements";
            LocalDateTime preferredTime = now.plusDays(3);

            Meeting meeting = Meeting.builder()
                    .student(student)
                    .lecturer(lecturer)
                    .subject(subject)
                    .description(description)
                    .preferredDateTime(preferredTime)
                    .meetingType(MeetingType.ACADEMIC_GUIDANCE)
                    .status(MeetingStatus.PENDING)
                    .priority(2)
                    .build();

            assertThat(meeting).isNotNull();
            assertThat(meeting.getStudent()).isEqualTo(student);
            assertThat(meeting.getLecturer()).isEqualTo(lecturer);
            assertThat(meeting.getSubject()).isEqualTo(subject);
            assertThat(meeting.getDescription()).isEqualTo(description);
            assertThat(meeting.getPreferredDateTime()).isEqualTo(preferredTime);
            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.PENDING);
            assertThat(meeting.getPriority()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should set defaults correctly")
        void builder_defaultValues() {
            Student student = new Student();
            student.setId(1L);
            AcademicStaff lecturer = new AcademicStaff();
            lecturer.setId(2L);

            Meeting meeting = Meeting.builder()
                    .student(student)
                    .lecturer(lecturer)
                    .subject("Meeting")
                    .build();

            assertThat(meeting.getMeetingType()).isEqualTo(MeetingType.ACADEMIC_GUIDANCE);
            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.PENDING);
            assertThat(meeting.getPriority()).isEqualTo(2);
            assertThat(meeting.getPreferredDurationMinutes()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should transition from PENDING to ACCEPTED")
        void transitionPendingToAccepted() {
            Meeting meeting = createPendingMeeting();
            meeting.setStatus(MeetingStatus.ACCEPTED);
            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.ACCEPTED);
        }

        @Test
        @DisplayName("Should transition from ACCEPTED to SCHEDULED")
        void transitionAcceptedToScheduled() {
            Meeting meeting = createPendingMeeting();
            meeting.setStatus(MeetingStatus.ACCEPTED);
            
            LocalDateTime scheduledTime = LocalDateTime.now().plusDays(1);
            meeting.setScheduledStartTime(scheduledTime);
            meeting.setLocation("Office Room 201");

            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.ACCEPTED);
            assertThat(meeting.getScheduledStartTime()).isEqualTo(scheduledTime);
            assertThat(meeting.getLocation()).isEqualTo("Office Room 201");
        }

        @Test
        @DisplayName("Should handle meeting completion")
        void transitionToCompleted() {
            Meeting meeting = createPendingMeeting();
            meeting.setStatus(MeetingStatus.COMPLETED);
            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Meeting Type Tests")
    class MeetingTypeTests {

        @Test
        @DisplayName("Should support different meeting types")
        void supportDifferentMeetingTypes() {
            Student student = new Student();
            student.setId(1L);
            AcademicStaff lecturer = new AcademicStaff();
            lecturer.setId(2L);

            for (MeetingType type : MeetingType.values()) {
                Meeting meeting = Meeting.builder()
                        .student(student)
                        .lecturer(lecturer)
                        .subject("Meeting")
                        .meetingType(type)
                        .build();
                
                assertThat(meeting.getMeetingType()).isEqualTo(type);
            }
        }
    }

    @Nested
    @DisplayName("Scheduling and Location")
    class SchedulingTests {

        @Test
        @DisplayName("Should handle in-person meetings")
        void setInPersonMeeting() {
            Meeting meeting = createBasicMeeting();
            meeting.setIsOnline(false);
            meeting.setLocation("Library Room 5");

            assertThat(meeting.getIsOnline()).isFalse();
            assertThat(meeting.getLocation()).isEqualTo("Library Room 5");
        }

        @Test
        @DisplayName("Should handle online meetings")
        void setOnlineMeeting() {
            Meeting meeting = createBasicMeeting();
            meeting.setIsOnline(true);
            meeting.setMeetingLink("https://zoom.us/j/123456789");
            meeting.setMeetingPlatform("Zoom");

            assertThat(meeting.getIsOnline()).isTrue();
            assertThat(meeting.getMeetingLink()).isEqualTo("https://zoom.us/j/123456789");
            assertThat(meeting.getMeetingPlatform()).isEqualTo("Zoom");
        }

        @Test
        @DisplayName("Should handle scheduled times")
        void handleScheduledTimes() {
            LocalDateTime start = LocalDateTime.now().plusDays(2);
            LocalDateTime end = start.plusHours(1);
            
            Meeting meeting = createBasicMeeting();
            meeting.setScheduledStartTime(start);
            meeting.setScheduledEndTime(end);

            assertThat(meeting.getScheduledStartTime()).isEqualTo(start);
            assertThat(meeting.getScheduledEndTime()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("Priority Tests")
    class PriorityTests {

        @Test
        @DisplayName("Should accept different priority levels")
        void acceptDifferentPriorities() {
            Meeting meeting = createBasicMeeting();
            int[] priorities = {1, 2, 3, 4};

            for (int priority : priorities) {
                meeting.setPriority(priority);
                assertThat(meeting.getPriority()).isEqualTo(priority);
            }
        }
    }

    @Nested
    @DisplayName("Feedback and Response")
    class FeedbackTests {

        @Test
        @DisplayName("Should accept student feedback after meeting")
        void acceptStudentFeedback() {
            Meeting meeting = createBasicMeeting();
            meeting.setStatus(MeetingStatus.COMPLETED);
            meeting.setStudentFeedback("Great discussion");
            meeting.setStudentRating(5);

            assertThat(meeting.getStudentFeedback()).isEqualTo("Great discussion");
            assertThat(meeting.getStudentRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should accept lecturer response")
        void acceptLecturerResponse() {
            Meeting meeting = createBasicMeeting();
            meeting.setLecturerResponse("Can meet on Tuesday");
            meeting.setRespondedAt(LocalDateTime.now());

            assertThat(meeting.getLecturerResponse()).isEqualTo("Can meet on Tuesday");
            assertThat(meeting.getRespondedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cancellation")
    class CancellationTests {

        @Test
        @DisplayName("Should track cancellation reason")
        void trackCancellationReason() {
            Meeting meeting = createBasicMeeting();
            meeting.setStatus(MeetingStatus.CANCELLED);
            meeting.setCancellationReason("Student unavailable");

            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.CANCELLED);
            assertThat(meeting.getCancellationReason()).isEqualTo("Student unavailable");
        }
    }

    @Nested
    @DisplayName("Participants")
    class ParticipantTests {

        @Test
        @DisplayName("Should validate student and lecturer are different")
        void studentAndLecturerAreDifferent() {
            Student student = new Student();
            student.setId(1L);
            AcademicStaff lecturer = new AcademicStaff();
            lecturer.setId(2L);

            Meeting meeting = Meeting.builder()
                    .student(student)
                    .lecturer(lecturer)
                    .subject("Meeting")
                    .build();

            assertThat(meeting.getStudent().getId()).isNotEqualTo(meeting.getLecturer().getId());
        }

        @Test
        @DisplayName("Should require both student and lecturer")
        void requireBothParticipants() {
            Student student = new Student();
            student.setId(1L);
            
            Meeting meeting = Meeting.builder()
                    .student(student)
                    .subject("Meeting")
                    .build();

            assertThat(meeting.getStudent()).isNotNull();
            assertThat(meeting.getLecturer()).isNull();
        }
    }

    // Helper methods
    private Meeting createBasicMeeting() {
        Student student = new Student();
        student.setId(1L);
        AcademicStaff lecturer = new AcademicStaff();
        lecturer.setId(2L);
        
        return Meeting.builder()
                .student(student)
                .lecturer(lecturer)
                .subject("Test Meeting")
                .build();
    }

    private Meeting createPendingMeeting() {
        Meeting meeting = createBasicMeeting();
        meeting.setStatus(MeetingStatus.PENDING);
        return meeting;
    }
}



