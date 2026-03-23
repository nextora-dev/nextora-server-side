package lk.iit.nextora.module.election.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.module.election.dto.response.CandidateResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResponse;
import lk.iit.nextora.module.election.dto.response.VotingStatisticsResponse;
import lk.iit.nextora.module.election.service.ElectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElectionAdminController Unit Tests")
class ElectionAdminControllerTest {

    @Mock private ElectionService electionService;

    @InjectMocks
    private ElectionAdminController controller;

    // ============================================================
    // ADMIN GET ALL ELECTIONS TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/admin/elections (get all elections with admin view)")
    class GetAllElectionsAdminTests {

        @Test
        @DisplayName("Should return all elections for admin successfully")
        void getAllElections_admin_success() {
            // Given
            PagedResponse<ElectionResponse> pagedResponse = PagedResponse.<ElectionResponse>builder()
                    .content(List.of(
                            ElectionResponse.builder().id(1L).clubId(1L).title("President Election").status(ElectionStatus.VOTING_OPEN).build(),
                            ElectionResponse.builder().id(2L).clubId(2L).title("Secretary Election").status(ElectionStatus.DRAFT).build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(electionService.getAllElectionsAdmin(any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<ElectionResponse>> response = controller.getAllElections(0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getTotalElements()).isEqualTo(2L);
            verify(electionService, times(1)).getAllElectionsAdmin(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no elections")
        void getAllElections_admin_returnsEmptyPage() {
            // Given
            PagedResponse<ElectionResponse> emptyResponse = PagedResponse.<ElectionResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();

            when(electionService.getAllElectionsAdmin(any(Pageable.class)))
                    .thenReturn(emptyResponse);

            // When
            ApiResponse<PagedResponse<ElectionResponse>> response = controller.getAllElections(0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().isEmpty()).isTrue();
            verify(electionService, times(1)).getAllElectionsAdmin(any(Pageable.class));
        }
    }

    // ============================================================
    // PERMANENT DELETE ELECTION TESTS
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/v1/admin/elections/{electionId}/permanent (permanently delete election)")
    class PermanentlyDeleteElectionTests {

        @Test
        @DisplayName("Should permanently delete election successfully")
        void permanentlyDeleteElection_success() {
            // Given
            Long electionId = 1L;
            doNothing().when(electionService).permanentlyDeleteElection(electionId);

            // When
            ApiResponse<Void> response = controller.permanentlyDeleteElection(electionId);

            // Then
            assertThat(response).isNotNull();
            verify(electionService, times(1)).permanentlyDeleteElection(electionId);
        }
    }

    // ============================================================
    // FORCE OPEN NOMINATIONS TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/admin/elections/{electionId}/force-open-nominations (force open nominations)")
    class ForceOpenNominationsTests {

        @Test
        @DisplayName("Should force open nominations successfully")
        void forceOpenNominations_success() {
            // Given
            Long electionId = 1L;
            ElectionResponse expectedResponse = ElectionResponse.builder()
                    .id(electionId)
                    .clubId(1L)
                    .title("President Election")
                    .status(ElectionStatus.NOMINATION_OPEN)
                    .build();

            when(electionService.forceOpenNominations(electionId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ElectionResponse> response = controller.forceOpenNominations(electionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getStatus()).isEqualTo(ElectionStatus.NOMINATION_OPEN);
            verify(electionService, times(1)).forceOpenNominations(electionId);
        }
    }

    // ============================================================
    // FORCE CLOSE NOMINATIONS TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/admin/elections/{electionId}/force-close-nominations (force close nominations)")
    class ForceCloseNominationsTests {

        @Test
        @DisplayName("Should force close nominations successfully")
        void forceCloseNominations_success() {
            // Given
            Long electionId = 1L;
            ElectionResponse expectedResponse = ElectionResponse.builder()
                    .id(electionId)
                    .clubId(1L)
                    .title("President Election")
                    .status(ElectionStatus.NOMINATION_CLOSED)
                    .build();

            when(electionService.forceCloseNominations(electionId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ElectionResponse> response = controller.forceCloseNominations(electionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getStatus()).isEqualTo(ElectionStatus.NOMINATION_CLOSED);
            verify(electionService, times(1)).forceCloseNominations(electionId);
        }
    }

    // ============================================================
    // FORCE OPEN VOTING TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/admin/elections/{electionId}/force-open-voting (force open voting)")
    class ForceOpenVotingTests {

        @Test
        @DisplayName("Should force open voting successfully")
        void forceOpenVoting_success() {
            // Given
            Long electionId = 1L;
            ElectionResponse expectedResponse = ElectionResponse.builder()
                    .id(electionId)
                    .clubId(1L)
                    .title("President Election")
                    .status(ElectionStatus.VOTING_OPEN)
                    .build();

            when(electionService.forceOpenVoting(electionId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ElectionResponse> response = controller.forceOpenVoting(electionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getStatus()).isEqualTo(ElectionStatus.VOTING_OPEN);
            verify(electionService, times(1)).forceOpenVoting(electionId);
        }
    }

    // ============================================================
    // FORCE CLOSE VOTING TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/admin/elections/{electionId}/force-close-voting (force close voting)")
    class ForceCloseVotingTests {

        @Test
        @DisplayName("Should force close voting successfully")
        void forceCloseVoting_success() {
            // Given
            Long electionId = 1L;
            ElectionResponse expectedResponse = ElectionResponse.builder()
                    .id(electionId)
                    .clubId(1L)
                    .title("President Election")
                    .status(ElectionStatus.VOTING_CLOSED)
                    .build();

            when(electionService.forceCloseVoting(electionId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ElectionResponse> response = controller.forceCloseVoting(electionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getStatus()).isEqualTo(ElectionStatus.VOTING_CLOSED);
            verify(electionService, times(1)).forceCloseVoting(electionId);
        }
    }

    // ============================================================
    // VOTING STATISTICS TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/admin/elections/statistics (get platform voting statistics)")
    class GetVotingStatisticsTests {

        @Test
        @DisplayName("Should return platform voting statistics successfully")
        void getPlatformStatistics_success() {
            // Given
            VotingStatisticsResponse expectedResponse = VotingStatisticsResponse.builder()
                    .totalVotesCast(85L)
                    .totalElections(10L)
                    .build();

            when(electionService.getPlatformStatistics())
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<VotingStatisticsResponse> response = controller.getPlatformStatistics();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getTotalVotesCast()).isEqualTo(85L);
            verify(electionService, times(1)).getPlatformStatistics();
        }
    }

    // ============================================================
    // GET CANDIDATES ADMIN TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/admin/elections/{electionId}/candidates (get all candidates)")
    class GetCandidatesAdminTests {

        @Test
        @DisplayName("Should return all candidates for election")
        void getAllCandidates_success() {
            // Given
            Long electionId = 1L;
            PagedResponse<CandidateResponse> pagedResponse = PagedResponse.<CandidateResponse>builder()
                    .content(List.of(
                            CandidateResponse.builder().id(1L).electionId(electionId).studentName("John Doe").voteCount(60).build(),
                            CandidateResponse.builder().id(2L).electionId(electionId).studentName("Jane Smith").voteCount(40).build(),
                            CandidateResponse.builder().id(3L).electionId(electionId).studentName("Bob Johnson").voteCount(0).build()
                    ))
                    .totalElements(3L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(electionService.getAllCandidatesAdmin(eq(electionId), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<CandidateResponse>> response = controller.getAllCandidates(electionId, 0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(3);
            verify(electionService, times(1)).getAllCandidatesAdmin(eq(electionId), any(Pageable.class));
        }
    }
}

