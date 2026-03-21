package lk.iit.nextora.module.election.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.CandidateStatus;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.enums.ElectionType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.election.dto.request.CastVoteRequest;
import lk.iit.nextora.module.election.dto.request.CreateElectionRequest;
import lk.iit.nextora.module.election.dto.request.NominateCandidateRequest;
import lk.iit.nextora.module.election.dto.response.CandidateResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResultsResponse;
import lk.iit.nextora.module.election.entity.Candidate;
import lk.iit.nextora.module.election.entity.Election;
import lk.iit.nextora.module.election.entity.Vote;
import lk.iit.nextora.module.election.repository.CandidateRepository;
import lk.iit.nextora.module.election.repository.ElectionRepository;
import lk.iit.nextora.module.election.repository.VoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElectionServiceImpl Unit Tests")
class ElectionServiceImplTest {

    @Mock
    private ElectionRepository electionRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ElectionServiceImpl electionService;

    @Nested
    @DisplayName("createElection")
    class CreateElectionTests {

        @Test
        @DisplayName("Should create election successfully")
        void createElection_success() {
            LocalDateTime now = LocalDateTime.now();
            CreateElectionRequest request = CreateElectionRequest.builder()
                    .clubId(1L)
                    .title("President Election 2026")
                    .description("Annual election for president position")
                    .electionType(ElectionType.GENERAL)
                    .nominationStartTime(now.plusDays(1))
                    .nominationEndTime(now.plusDays(8))
                    .votingStartTime(now.plusDays(9))
                    .votingEndTime(now.plusDays(10))
                    .build();

            Club club = Club.builder().id(1L).clubCode("TECH001").name("Tech Club").build();
            Election electionEntity = Election.builder().id(1L).club(club).title("President Election 2026").status(ElectionStatus.DRAFT).build();

            when(clubRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(club));
            when(electionRepository.save(any(Election.class))).thenReturn(electionEntity);

            ElectionResponse response = electionService.createElection(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            verify(electionRepository, times(1)).save(any(Election.class));
        }

        @Test
        @DisplayName("Should throw exception when club not found")
        void createElection_clubNotFound_throwsException() {
            CreateElectionRequest request = CreateElectionRequest.builder().clubId(999L).title("President Election").build();
            when(clubRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> electionService.createElection(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getElectionById")
    class GetElectionByIdTests {

        @Test
        @DisplayName("Should return election by ID successfully")
        void getElectionById_success() {
            Long electionId = 1L;
            Election election = Election.builder().id(electionId).title("President Election 2026").status(ElectionStatus.NOMINATION_OPEN).build();

            when(electionRepository.findByIdAndIsDeletedFalse(electionId)).thenReturn(Optional.of(election));

            ElectionResponse response = electionService.getElectionById(electionId);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(electionId);
            verify(electionRepository, times(1)).findByIdAndIsDeletedFalse(electionId);
        }

        @Test
        @DisplayName("Should throw exception when election not found")
        void getElectionById_notFound_throwsException() {
            when(electionRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> electionService.getElectionById(999L)).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getUpcomingElections")
    class GetAllElectionsTests {

        @Test
        @DisplayName("Should return paginated upcoming elections")
        void getUpcomingElections_returnsPaginatedResult() {
            Pageable pageable = PageRequest.of(0, 10);
            List<Election> elections = List.of(
                    Election.builder().id(1L).title("President Election").status(ElectionStatus.VOTING_OPEN).build(),
                    Election.builder().id(2L).title("Secretary Election").status(ElectionStatus.NOMINATION_OPEN).build()
            );
            Page<Election> electionPage = new PageImpl<>(elections, pageable, 2);

            when(electionRepository.findAllUpcoming(any(LocalDateTime.class), eq(pageable))).thenReturn(electionPage);

            PagedResponse<ElectionResponse> response = electionService.getUpcomingElections(pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            verify(electionRepository, times(1)).findAllUpcoming(any(LocalDateTime.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("nominateSelf")
    class NominateCandidateTests {

        @Test
        @DisplayName("Should nominate self as candidate successfully")
        void nominateSelf_success() {
            NominateCandidateRequest request = NominateCandidateRequest.builder().build();
            Candidate candidate = Candidate.builder().id(1L).manifesto("I will lead").voteCount(0).build();

            when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

            CandidateResponse response = electionService.nominateSelf(request, null);

            assertThat(response).isNotNull();
            verify(candidateRepository, times(1)).save(any(Candidate.class));
        }

        @Test
        @DisplayName("Should throw exception when nomination details invalid")
        void nominateSelf_invalidDetails_throwsException() {
            NominateCandidateRequest request = NominateCandidateRequest.builder().build();

            assertThatThrownBy(() -> electionService.nominateSelf(request, null))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("castVote")
    class CastVoteTests {

        @Test
        @DisplayName("Should cast vote successfully")
        void castVote_success() {
            CastVoteRequest request = CastVoteRequest.builder().candidateId(1L).build();
            
            var response = electionService.castVote(request, "192.168.1.1", "Mozilla/5.0");

            assertThat(response).isNotNull();
            verify(voteRepository, times(1)).save(any(Vote.class));
        }

        @Test
        @DisplayName("Should throw exception when voting not open")
        void castVote_votingNotOpen_throwsException() {
            CastVoteRequest request = CastVoteRequest.builder().candidateId(1L).build();

            assertThatThrownBy(() -> electionService.castVote(request, "192.168.1.1", "Mozilla/5.0"))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("getElectionResults")
    class GetElectionResultsTests {

        @Test
        @DisplayName("Should return election results successfully")
        void getElectionResults_success() {
            Long electionId = 1L;
            Election election = Election.builder().id(electionId).title("President Election 2026").status(ElectionStatus.RESULTS_PUBLISHED).build();

            when(electionRepository.findByIdAndIsDeletedFalse(electionId)).thenReturn(Optional.of(election));

            ElectionResultsResponse response = electionService.getElectionResults(electionId);

            assertThat(response).isNotNull();
            assertThat(response.getElectionTitle()).isEqualTo("President Election 2026");
            verify(electionRepository, times(1)).findByIdAndIsDeletedFalse(electionId);
        }
    }

    @Nested
    @DisplayName("getLiveVoteCount")
    class GetVotingStatisticsTests {

        @Test
        @DisplayName("Should return live vote count during voting")
        void getLiveVoteCount_success() {
            Long electionId = 1L;
            Election election = Election.builder().id(electionId).title("President Election").status(ElectionStatus.VOTING_CLOSED).build();

            when(electionRepository.findByIdAndIsDeletedFalse(electionId)).thenReturn(Optional.of(election));

            ElectionResultsResponse response = electionService.getLiveVoteCount(electionId);

            assertThat(response).isNotNull();
            verify(electionRepository, times(1)).findByIdAndIsDeletedFalse(electionId);
        }
    }

    @Nested
    @DisplayName("getCandidates")
    class GetCandidatesTests {

        @Test
        @DisplayName("Should return paginated candidates for election")
        void getCandidates_returnsPaginatedResult() {
            Long electionId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            List<Candidate> candidates = List.of(
                    Candidate.builder().id(1L).voteCount(60).status(CandidateStatus.APPROVED).build(),
                    Candidate.builder().id(2L).voteCount(40).status(CandidateStatus.APPROVED).build()
            );
            Page<Candidate> candidatePage = new PageImpl<>(candidates, pageable, 2);

            when(candidateRepository.findByElectionIdAndIsDeletedFalse(electionId, pageable)).thenReturn(candidatePage);

            PagedResponse<CandidateResponse> response = electionService.getCandidates(electionId, pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            verify(candidateRepository, times(1)).findByElectionIdAndIsDeletedFalse(electionId, pageable);
        }
    }
}

