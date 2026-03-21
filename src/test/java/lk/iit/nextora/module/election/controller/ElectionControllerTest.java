package lk.iit.nextora.module.election.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.election.dto.request.CastVoteRequest;
import lk.iit.nextora.module.election.dto.request.CreateElectionRequest;
import lk.iit.nextora.module.election.dto.request.NominateCandidateRequest;
import lk.iit.nextora.module.election.dto.response.CandidateResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResultsResponse;
import lk.iit.nextora.module.election.dto.response.VoteResponse;
import lk.iit.nextora.module.election.service.ElectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElectionController Unit Tests")
class ElectionControllerTest {

    @Mock private ElectionService electionService;

    @InjectMocks
    private ElectionController controller;

    // ============================================================
    // CREATE ELECTION TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/elections (create election)")
    class CreateElectionTests {

        @Test
        @DisplayName("Should create election successfully")
        void createElection_success() {
            // Given
            CreateElectionRequest request = CreateElectionRequest.builder()
                    .clubId(1L)
                    .title("President Election 2026")
                    .description("Annual president election for tech club")
                    .nominationStartTime(LocalDateTime.now().plusDays(1))
                    .nominationEndTime(LocalDateTime.now().plusDays(8))
                    .votingStartTime(LocalDateTime.now().plusDays(9))
                    .votingEndTime(LocalDateTime.now().plusDays(10))
                    .build();

            ElectionResponse expectedResponse = ElectionResponse.builder()
                    .id(1L)
                    .clubId(1L)
                    .title("President Election 2026")
                    .status(ElectionStatus.DRAFT)
                    .build();

            when(electionService.createElection(any(CreateElectionRequest.class)))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ElectionResponse> response = controller.createElection(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getId()).isEqualTo(1L);
            assertThat(response.getData().getTitle()).isEqualTo("President Election 2026");
            verify(electionService, times(1)).createElection(any(CreateElectionRequest.class));
        }
    }

    // ============================================================
    // GET ELECTION TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/elections/{electionId} (get election by ID)")
    class GetElectionByIdTests {

        @Test
        @DisplayName("Should return election by ID successfully")
        void getElectionById_success() {
            // Given
            Long electionId = 1L;
            ElectionResponse expectedResponse = ElectionResponse.builder()
                    .id(electionId)
                    .clubId(1L)
                    .title("President Election 2026")
                    .status(ElectionStatus.NOMINATION_OPEN)
                    .build();

            when(electionService.getElectionById(electionId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ElectionResponse> response = controller.getElectionById(electionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isEqualTo(expectedResponse);
            verify(electionService, times(1)).getElectionById(electionId);
        }

        @Test
        @DisplayName("Should throw exception when election not found")
        void getElectionById_notFound() {
            // Given
            Long electionId = 999L;
            when(electionService.getElectionById(electionId))
                    .thenThrow(new ResourceNotFoundException("Election", "id", electionId.toString()));

            // When & Then
            assertThatThrownBy(() -> controller.getElectionById(electionId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(electionService, times(1)).getElectionById(electionId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/elections (get all elections)")
    class GetAllElectionsTests {

        @Test
        @DisplayName("Should return paginated elections")
        void getAllElections_returnsPaginatedResult() {
            // Given
            PagedResponse<ElectionResponse> pagedResponse = PagedResponse.<ElectionResponse>builder()
                    .content(List.of(
                            ElectionResponse.builder().id(1L).clubId(1L).title("President Election 2026").build(),
                            ElectionResponse.builder().id(2L).clubId(2L).title("Secretary Election 2026").build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(electionService.getAllElections(any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<ElectionResponse>> response = controller.getAllElections(0, 10, "title", "ASC");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getTotalElements()).isEqualTo(2L);
            verify(electionService, times(1)).getAllElections(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no elections exist")
        void getAllElections_returnsEmptyResult() {
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

            when(electionService.getAllElections(any(Pageable.class)))
                    .thenReturn(emptyResponse);

            // When
            ApiResponse<PagedResponse<ElectionResponse>> response = controller.getAllElections(0, 10, "title", "ASC");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().isEmpty()).isTrue();
            verify(electionService, times(1)).getAllElections(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clubs/{clubId}/elections (get elections by club)")
    class GetElectionsByClubTests {

        @Test
        @DisplayName("Should return elections for club successfully")
        void getElectionsByClub_success() {
            // Given
            Long clubId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            PagedResponse<ElectionResponse> pagedResponse = PagedResponse.<ElectionResponse>builder()
                    .content(List.of(
                            ElectionResponse.builder().id(1L).clubId(clubId).title("President Election 2026").build(),
                            ElectionResponse.builder().id(2L).clubId(clubId).title("Vice President Election 2026").build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(electionService.getElectionsByClub(eq(clubId), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<ElectionResponse>> response = controller.getElectionsByClub(clubId, 0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getContent()).allMatch(e -> e.getClubId().equals(clubId));
            verify(electionService, times(1)).getElectionsByClub(eq(clubId), any(Pageable.class));
        }
    }

    // ============================================================
    // NOMINATION TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/elections/{electionId}/candidates (nominate candidate)")
    class NominateCandidateTests {

        @Test
        @DisplayName("Should nominate candidate successfully")
        void nominateCandidate_success() {
            // Given
            Long electionId = 1L;
            NominateCandidateRequest request = NominateCandidateRequest.builder()
                    .studentId(1L)
                    .position("PRESIDENT")
                    .manifesto("I will lead the club to success")
                    .build();

            CandidateResponse expectedResponse = CandidateResponse.builder()
                    .id(1L)
                    .electionId(electionId)
                    .studentId(1L)
                    .studentName("John Doe")
                    .position("PRESIDENT")
                    .voteCount(0L)
                    .build();

            when(electionService.nominateCandidate(eq(electionId), any(NominateCandidateRequest.class)))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<CandidateResponse> response = controller.nominateCandidate(electionId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getPosition()).isEqualTo("PRESIDENT");
            verify(electionService, times(1)).nominateCandidate(eq(electionId), any(NominateCandidateRequest.class));
        }
    }

    // ============================================================
    // VOTING TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/elections/{electionId}/vote (cast vote)")
    class CastVoteTests {

        @Test
        @DisplayName("Should cast vote successfully")
        void castVote_success() {
            // Given
            Long electionId = 1L;
            CastVoteRequest request = CastVoteRequest.builder()
                    .candidateId(1L)
                    .build();

            VoteResponse expectedResponse = VoteResponse.builder()
                    .id(1L)
                    .electionId(electionId)
                    .candidateId(1L)
                    .build();

            when(electionService.castVote(eq(electionId), any(CastVoteRequest.class)))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<VoteResponse> response = controller.castVote(electionId, request, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getElectionId()).isEqualTo(electionId);
            verify(electionService, times(1)).castVote(eq(electionId), any(CastVoteRequest.class));
        }
    }

    // ============================================================
    // ELECTION RESULTS TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/elections/{electionId}/results (get election results)")
    class GetElectionResultsTests {

        @Test
        @DisplayName("Should return election results successfully")
        void getElectionResults_success() {
            // Given
            Long electionId = 1L;
            ElectionResultsResponse expectedResponse = ElectionResultsResponse.builder()
                    .electionId(electionId)
                    .electionTitle("President Election 2026")
                    .totalVotes(100L)
                    .winner("John Doe")
                    .winnerVotes(60L)
                    .build();

            when(electionService.getElectionResults(electionId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ElectionResultsResponse> response = controller.getElectionResults(electionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getWinner()).isEqualTo("John Doe");
            assertThat(response.getData().getWinnerVotes()).isEqualTo(60L);
            verify(electionService, times(1)).getElectionResults(electionId);
        }
    }

    // ============================================================
    // GET CANDIDATES TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/elections/{electionId}/candidates (get candidates)")
    class GetCandidatesTests {

        @Test
        @DisplayName("Should return candidates for election successfully")
        void getCandidates_success() {
            // Given
            Long electionId = 1L;
            PagedResponse<CandidateResponse> pagedResponse = PagedResponse.<CandidateResponse>builder()
                    .content(List.of(
                            CandidateResponse.builder().id(1L).electionId(electionId).studentName("John Doe").voteCount(60L).build(),
                            CandidateResponse.builder().id(2L).electionId(electionId).studentName("Jane Smith").voteCount(40L).build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(electionService.getCandidates(eq(electionId), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<CandidateResponse>> response = controller.getCandidates(electionId, 0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            verify(electionService, times(1)).getCandidates(eq(electionId), any(Pageable.class));
        }
    }
}

