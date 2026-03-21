package lk.iit.nextora.module.election.entity;

import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.enums.ElectionType;
import lk.iit.nextora.module.club.entity.Club;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Election Entity Unit Tests")
class ElectionTest {

    @Nested
    @DisplayName("Election Builder")
    class ElectionBuilderTests {

        @Test
        @DisplayName("Should create election with all fields")
        void buildElection_withAllFields_success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Club club = Club.builder().id(1L).clubCode("TECH001").name("Tech Club").build();

            // When
            Election election = Election.builder()
                    .id(1L)
                    .clubId(1L)
                    .club(club)
                    .title("President Election 2026")
                    .description("Annual election for president")
                    .electionType(ElectionType.GENERAL)
                    .status(ElectionStatus.DRAFT)
                    .nominationStartTime(now.plusDays(1))
                    .nominationEndTime(now.plusDays(8))
                    .votingStartTime(now.plusDays(9))
                    .votingEndTime(now.plusDays(10))
                    .isActive(true)
                    .build();

            // Then
            assertThat(election).isNotNull();
            assertThat(election.getId()).isEqualTo(1L);
            assertThat(election.getTitle()).isEqualTo("President Election 2026");
            assertThat(election.getStatus()).isEqualTo(ElectionStatus.DRAFT);
            assertThat(election.getElectionType()).isEqualTo(ElectionType.GENERAL);
            assertThat(election.getClub()).isEqualTo(club);
        }

        @Test
        @DisplayName("Should create election with defaults")
        void buildElection_withDefaults_success() {
            // When
            Election election = Election.builder()
                    .id(1L)
                    .clubId(1L)
                    .title("Secretary Election")
                    .nominationStartTime(LocalDateTime.now())
                    .nominationEndTime(LocalDateTime.now().plusDays(1))
                    .votingStartTime(LocalDateTime.now().plusDays(2))
                    .votingEndTime(LocalDateTime.now().plusDays(3))
                    .build();

            // Then
            assertThat(election).isNotNull();
            assertThat(election.getStatus()).isEqualTo(ElectionStatus.DRAFT);
            assertThat(election.getElectionType()).isEqualTo(ElectionType.GENERAL);
        }
    }

    @Nested
    @DisplayName("Election Status Transitions")
    class ElectionStatusTests {

        @Test
        @DisplayName("Should validate election status progression")
        void electionStatusProgression() {
            // When
            Election election = Election.builder()
                    .id(1L)
                    .title("Test Election")
                    .nominationStartTime(LocalDateTime.now())
                    .nominationEndTime(LocalDateTime.now().plusDays(1))
                    .votingStartTime(LocalDateTime.now().plusDays(2))
                    .votingEndTime(LocalDateTime.now().plusDays(3))
                    .status(ElectionStatus.DRAFT)
                    .build();

            // Then - Initial status
            assertThat(election.getStatus()).isEqualTo(ElectionStatus.DRAFT);

            // Change status
            election.setStatus(ElectionStatus.NOMINATION_OPEN);
            assertThat(election.getStatus()).isEqualTo(ElectionStatus.NOMINATION_OPEN);

            election.setStatus(ElectionStatus.NOMINATION_CLOSED);
            assertThat(election.getStatus()).isEqualTo(ElectionStatus.NOMINATION_CLOSED);

            election.setStatus(ElectionStatus.VOTING_OPEN);
            assertThat(election.getStatus()).isEqualTo(ElectionStatus.VOTING_OPEN);
        }
    }

    @Nested
    @DisplayName("Election Types")
    class ElectionTypeTests {

        @Test
        @DisplayName("Should support different election types")
        void electionTypes() {
            // Test GENERAL election type
            Election generalElection = Election.builder()
                    .id(1L)
                    .title("General Election")
                    .electionType(ElectionType.GENERAL)
                    .nominationStartTime(LocalDateTime.now())
                    .nominationEndTime(LocalDateTime.now().plusDays(1))
                    .votingStartTime(LocalDateTime.now().plusDays(2))
                    .votingEndTime(LocalDateTime.now().plusDays(3))
                    .build();

            assertThat(generalElection.getElectionType()).isEqualTo(ElectionType.GENERAL);

            // Test SPECIAL election type
            Election specialElection = Election.builder()
                    .id(2L)
                    .title("Special Election")
                    .electionType(ElectionType.SPECIAL)
                    .nominationStartTime(LocalDateTime.now())
                    .nominationEndTime(LocalDateTime.now().plusDays(1))
                    .votingStartTime(LocalDateTime.now().plusDays(2))
                    .votingEndTime(LocalDateTime.now().plusDays(3))
                    .build();

            assertThat(specialElection.getElectionType()).isEqualTo(ElectionType.SPECIAL);
        }
    }

    @Nested
    @DisplayName("Election Validation")
    class ElectionValidationTests {

        @Test
        @DisplayName("Should validate required fields")
        void validateRequiredFields() {
            // When
            Election election = Election.builder()
                    .id(1L)
                    .clubId(1L)
                    .title("President Election")
                    .nominationStartTime(LocalDateTime.now())
                    .nominationEndTime(LocalDateTime.now().plusDays(1))
                    .votingStartTime(LocalDateTime.now().plusDays(2))
                    .votingEndTime(LocalDateTime.now().plusDays(3))
                    .build();

            // Then
            assertThat(election.getTitle()).isNotNull();
            assertThat(election.getTitle()).isNotEmpty();
            assertThat(election.getNominationStartTime()).isNotNull();
            assertThat(election.getNominationEndTime()).isNotNull();
            assertThat(election.getVotingStartTime()).isNotNull();
            assertThat(election.getVotingEndTime()).isNotNull();
        }
    }
}

