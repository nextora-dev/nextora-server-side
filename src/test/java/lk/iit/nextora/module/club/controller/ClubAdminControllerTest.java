package lk.iit.nextora.module.club.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.module.club.dto.request.ChangeMemberPositionRequest;
import lk.iit.nextora.module.club.dto.response.ClubActivityLogResponse;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubStatisticsResponse;
import lk.iit.nextora.module.club.entity.ClubActivityLog;
import lk.iit.nextora.module.club.service.ClubActivityLogService;
import lk.iit.nextora.module.club.service.ClubAnnouncementService;
import lk.iit.nextora.module.club.service.ClubService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClubAdminController Unit Tests")
class ClubAdminControllerTest {

    @Mock private ClubService clubService;
    @Mock private ClubActivityLogService activityLogService;
    @Mock private ClubAnnouncementService announcementService;

    @InjectMocks
    private ClubAdminController controller;

    // ============================================================
    // CLUB STATISTICS TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/clubs/admin/{clubId}/stats (get club statistics)")
    class GetClubStatisticsTests {

        @Test
        @DisplayName("Should return club statistics successfully")
        void getClubStatistics_success() {
            // Given
            Long clubId = 1L;
            ClubStatisticsResponse expectedResponse = ClubStatisticsResponse.builder()
                    .clubId(clubId)
                    .clubName("Tech Club")
                    .totalMembers(50L)
                    .activeMembers(45L)
                    .totalElections(5L)
                    .activeElections(1L)
                    .build();

            when(clubService.getClubStatistics(clubId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ClubStatisticsResponse> response = controller.getClubStatistics(clubId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getTotalMembers()).isEqualTo(50L);
            assertThat(response.getData().getActiveMembers()).isEqualTo(45L);
            verify(clubService, times(1)).getClubStatistics(clubId);
        }
    }

    // ============================================================
    // ACTIVITY LOG TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/clubs/admin/{clubId}/activity-logs (get activity logs)")
    class GetActivityLogsTests {

        @Test
        @DisplayName("Should return paginated activity logs")
        void getActivityLogs_returnsPaginatedResult() {
            // Given
            Long clubId = 1L;
            PagedResponse<ClubActivityLogResponse> pagedResponse = PagedResponse.<ClubActivityLogResponse>builder()
                    .content(List.of(
                            ClubActivityLogResponse.builder()
                                    .id(1L)
                                    .clubId(clubId)
                                    .activityType(ClubActivityLog.ActivityType.MEMBER_JOINED)
                                    .description("Student joined club")
                                    .build(),
                            ClubActivityLogResponse.builder()
                                    .id(2L)
                                    .clubId(clubId)
                                    .activityType(ClubActivityLog.ActivityType.MEMBER_JOINED)
                                    .description("Another student joined")
                                    .build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(20)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(activityLogService.getActivityLogs(eq(clubId), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<ClubActivityLogResponse>> response = controller.getActivityLogs(clubId, 0, 20);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getTotalElements()).isEqualTo(2L);
            verify(activityLogService, times(1)).getActivityLogs(eq(clubId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty activity logs")
        void getActivityLogs_returnsEmptyResult() {
            // Given
            Long clubId = 1L;
            PagedResponse<ClubActivityLogResponse> emptyResponse = PagedResponse.<ClubActivityLogResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .pageNumber(0)
                    .pageSize(20)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();

            when(activityLogService.getActivityLogs(eq(clubId), any(Pageable.class)))
                    .thenReturn(emptyResponse);

            // When
            ApiResponse<PagedResponse<ClubActivityLogResponse>> response = controller.getActivityLogs(clubId, 0, 20);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().isEmpty()).isTrue();
            verify(activityLogService, times(1)).getActivityLogs(eq(clubId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clubs/admin/{clubId}/activity-logs/type/{type} (get activity logs by type)")
    class GetActivityLogsByTypeTests {

        @Test
        @DisplayName("Should return activity logs filtered by type")
        void getActivityLogsByType_returnFilteredLogs() {
            // Given
            Long clubId = 1L;
            ClubActivityLog.ActivityType type = ClubActivityLog.ActivityType.MEMBER_JOINED;

            PagedResponse<ClubActivityLogResponse> pagedResponse = PagedResponse.<ClubActivityLogResponse>builder()
                    .content(List.of(
                            ClubActivityLogResponse.builder()
                                    .id(1L)
                                    .clubId(clubId)
                                    .activityType(type)
                                    .description("Student joined club")
                                    .build(),
                            ClubActivityLogResponse.builder()
                                    .id(3L)
                                    .clubId(clubId)
                                    .activityType(type)
                                    .description("Another student joined")
                                    .build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(20)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(activityLogService.getActivityLogsByType(eq(clubId), eq(type), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<ClubActivityLogResponse>> response = controller.getActivityLogsByType(clubId, type, 0, 20);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getContent()).allMatch(log -> log.getActivityType().equals(type));
            verify(activityLogService, times(1)).getActivityLogsByType(clubId, type, any(Pageable.class));
        }
    }

    // ============================================================
    // MEMBERSHIP MANAGEMENT TESTS
    // ============================================================

    @Nested
    @DisplayName("PUT /api/v1/clubs/memberships/{membershipId}/position (change member position)")
    class ChangeMemberPositionTests {

        @Test
        @DisplayName("Should change member position successfully")
        void changeMemberPosition_success() {
            // Given
            Long membershipId = 1L;
            ChangeMemberPositionRequest request = ChangeMemberPositionRequest.builder()
                    .newPosition(ClubPositionsType.VICE_PRESIDENT)
                    .reason("Promotion")
                    .build();

            ClubMembershipResponse expectedResponse = ClubMembershipResponse.builder()
                    .id(membershipId)
                    .clubId(1L)
                    .memberId(1L)
                    .position(ClubPositionsType.VICE_PRESIDENT)
                    .build();

            when(clubService.changeMemberPosition(eq(membershipId), any(ClubPositionsType.class), anyString()))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ClubMembershipResponse> response = controller.changeMemberPosition(membershipId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getPosition()).isEqualTo(ClubPositionsType.VICE_PRESIDENT);
            verify(clubService, times(1)).changeMemberPosition(eq(membershipId), any(ClubPositionsType.class), anyString());
        }
    }

    // ============================================================
    // BULK OPERATIONS TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/clubs/{clubId}/memberships/bulk-approve (bulk approve memberships)")
    class BulkApproveMembershipsTests {

        @Test
        @DisplayName("Should bulk approve memberships successfully")
        void bulkApproveMemberships_success() {
            // Given
            Long clubId = 1L;
            List<Long> membershipIds = List.of(1L, 2L, 3L);
            List<ClubMembershipResponse> expectedResponses = List.of(
                    ClubMembershipResponse.builder().id(1L).clubId(clubId).memberId(1L).build(),
                    ClubMembershipResponse.builder().id(2L).clubId(clubId).memberId(2L).build(),
                    ClubMembershipResponse.builder().id(3L).clubId(clubId).memberId(3L).build()
            );

            when(clubService.bulkApproveMemberships(clubId, membershipIds))
                    .thenReturn(expectedResponses);

            // When
            ApiResponse<List<ClubMembershipResponse>> response = controller.bulkApproveMemberships(clubId, membershipIds);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(3);
            verify(clubService, times(1)).bulkApproveMemberships(clubId, membershipIds);
        }
    }
}

