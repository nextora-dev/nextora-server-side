package lk.iit.nextora.module.meeting.service.impl;

import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.meeting.dto.request.CreateMeetingRequest;
import lk.iit.nextora.module.meeting.dto.response.MeetingResponse;
import lk.iit.nextora.module.meeting.entity.Meeting;
import lk.iit.nextora.module.meeting.mapper.MeetingMapper;
import lk.iit.nextora.module.meeting.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingServiceImpl Unit Tests")
class MeetingServiceImplTest {

    @Mock private MeetingRepository meetingRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AcademicStaffRepository academicStaffRepository;
    @Mock private MeetingMapper meetingMapper;
    @Mock private SecurityService securityService;

    @InjectMocks private MeetingServiceImpl meetingService;

    private Student student;
    private AcademicStaff lecturer;
    private Meeting meeting;
    private MeetingResponse response;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");

        lecturer = new AcademicStaff();
        lecturer.setId(2L);
        lecturer.setFirstName("Prof");
        lecturer.setLastName("Smith");
        lecturer.setAvailableForMeetings(true);

        meeting = Meeting.builder()
                .student(student)
                .lecturer(lecturer)
                .status(MeetingStatus.PENDING)
                .build();
        meeting.setId(1L);

        response = MeetingResponse.builder()
                .id(1L)
                .status(MeetingStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("getMeetingById")
    class GetMeetingByIdTests {

        @Test
        @DisplayName("Should retrieve meeting by id")
        void getMeetingById_success() {
            when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
            when(meetingMapper.toResponse(meeting)).thenReturn(response);

            MeetingResponse result = meetingService.getMeetingById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(meetingRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw when meeting not found")
        void getMeetingById_notFound() {
            when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> meetingService.getMeetingById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createMeetingRequest")
    class CreateMeetingRequestTests {

        @Test
        @DisplayName("Should create meeting request successfully")
        void createMeetingRequest_success() {
            CreateMeetingRequest request = new CreateMeetingRequest();
            request.setLecturerId(2L);
            request.setPriority(1);
            request.setDescription("Need guidance on assignment");

            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(academicStaffRepository.findById(2L)).thenReturn(Optional.of(lecturer));
            when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);
            when(meetingMapper.toResponse(meeting)).thenReturn(response);

            MeetingResponse result = meetingService.createMeetingRequest(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(meetingRepository).save(any(Meeting.class));
        }

        @Test
        @DisplayName("Should throw when student not found")
        void createMeetingRequest_studentNotFound() {
            CreateMeetingRequest request = new CreateMeetingRequest();
            request.setLecturerId(2L);

            when(securityService.getCurrentUserId()).thenReturn(99L);
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> meetingService.createMeetingRequest(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when lecturer not found")
        void createMeetingRequest_lecturerNotFound() {
            CreateMeetingRequest request = new CreateMeetingRequest();
            request.setLecturerId(99L);

            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(academicStaffRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> meetingService.createMeetingRequest(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
