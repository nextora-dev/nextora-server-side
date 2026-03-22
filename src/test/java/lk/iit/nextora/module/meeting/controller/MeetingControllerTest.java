package lk.iit.nextora.module.meeting.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.module.meeting.dto.request.AcceptMeetingRequest;
import lk.iit.nextora.module.meeting.dto.request.CreateMeetingRequest;
import lk.iit.nextora.module.meeting.dto.request.RejectMeetingRequest;
import lk.iit.nextora.module.meeting.dto.response.MeetingResponse;
import lk.iit.nextora.module.meeting.service.MeetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingController Unit Tests")
class MeetingControllerTest {

    @Mock private MeetingService meetingService;

    @InjectMocks private MeetingController controller;

    @Nested
    @DisplayName("createMeetingRequest")
    class CreateMeetingRequestTests {

        @Test
        @DisplayName("Should submit meeting request successfully")
        void createMeetingRequest_success() {
            CreateMeetingRequest request = new CreateMeetingRequest();
            request.setLecturerId(2L);
            request.setPriority(1);
            request.setDescription("Need guidance");

            MeetingResponse response = MeetingResponse.builder()
                    .id(1L)
                    .status(MeetingStatus.PENDING)
                    .build();

            when(meetingService.createMeetingRequest(any(CreateMeetingRequest.class)))
                    .thenReturn(response);

            ApiResponse<MeetingResponse> result = 
                    controller.createMeetingRequest(request);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getStatus()).isEqualTo(MeetingStatus.PENDING);
            verify(meetingService).createMeetingRequest(any(CreateMeetingRequest.class));
        }
    }

    @Nested
    @DisplayName("getMeetingById")
    class GetMeetingByIdTests {

        @Test
        @DisplayName("Should retrieve meeting by id")
        void getMeetingById_success() {
            MeetingResponse response = MeetingResponse.builder()
                    .id(1L)
                    .status(MeetingStatus.PENDING)
                    .build();

            when(meetingService.getMeetingById(1L))
                    .thenReturn(response);

            ApiResponse<MeetingResponse> result = 
                    controller.getMeetingById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getId()).isEqualTo(1L);
            verify(meetingService).getMeetingById(1L);
        }

        @Test
        @DisplayName("Should throw when meeting not found")
        void getMeetingById_notFound() {
            when(meetingService.getMeetingById(99L))
                    .thenThrow(new RuntimeException("Meeting not found"));

            assertThatThrownBy(() -> controller.getMeetingById(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("acceptMeetingRequest")
    class AcceptMeetingRequestTests {

        @Test
        @DisplayName("Should accept meeting request with decision details")
        void acceptMeetingRequest_success() {
            AcceptMeetingRequest request = new AcceptMeetingRequest();
            
            MeetingResponse response = MeetingResponse.builder()
                    .id(1L)
                    .status(MeetingStatus.SCHEDULED)
                    .build();

            when(meetingService.acceptMeetingRequest(eq(1L), any(AcceptMeetingRequest.class)))
                    .thenReturn(response);

            ApiResponse<MeetingResponse> result = 
                    controller.acceptMeetingRequest(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getStatus()).isEqualTo(MeetingStatus.SCHEDULED);
            verify(meetingService).acceptMeetingRequest(eq(1L), any(AcceptMeetingRequest.class));
        }
    }

    @Nested
    @DisplayName("rejectMeetingRequest")
    class RejectMeetingRequestTests {

        @Test
        @DisplayName("Should reject meeting request with reason")
        void rejectMeetingRequest_success() {
            RejectMeetingRequest request = new RejectMeetingRequest();
            request.setReason("Lecturer unavailable");
            
            MeetingResponse response = MeetingResponse.builder()
                    .id(1L)
                    .status(MeetingStatus.REJECTED)
                    .build();

            when(meetingService.rejectMeetingRequest(eq(1L), any(RejectMeetingRequest.class)))
                    .thenReturn(response);

            ApiResponse<MeetingResponse> result = 
                    controller.rejectMeetingRequest(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getStatus()).isEqualTo(MeetingStatus.REJECTED);
            verify(meetingService).rejectMeetingRequest(eq(1L), any(RejectMeetingRequest.class));
        }
    }
}
