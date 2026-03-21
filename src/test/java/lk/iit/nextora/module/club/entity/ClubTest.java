package lk.iit.nextora.module.club.entity;

import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.module.auth.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Club Entity Unit Tests")
class ClubTest {

    @Nested
    @DisplayName("canAcceptMembers")
    class CanAcceptMembersTests {

        @Test
        @DisplayName("Should return true when registration is open and club is active")
        void canAcceptMembers_registrationOpenAndActive_returnsTrue() {
            // Given
            Club club = Club.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .isRegistrationOpen(true)
                    .isActive(true)
                    .isDeleted(false)
                    .build();

            // When
            boolean result = club.canAcceptMembers();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when registration is closed")
        void canAcceptMembers_registrationClosed_returnsFalse() {
            // Given
            Club club = Club.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .isRegistrationOpen(false)
                    .isActive(true)
                    .isDeleted(false)
                    .build();

            // When
            boolean result = club.canAcceptMembers();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when club is inactive")
        void canAcceptMembers_clubInactive_returnsFalse() {
            // Given
            Club club = Club.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .isRegistrationOpen(true)
                    .isActive(false)
                    .isDeleted(false)
                    .build();

            // When
            boolean result = club.canAcceptMembers();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Club Builder and Fields")
    class ClubBuilderTests {

        @Test
        @DisplayName("Should create club with all fields")
        void clubBuilder_withAllFields_success() {
            // Given
            LocalDate establishedDate = LocalDate.of(2020, 1, 1);
            Student president = Student.builder()
                    .studentId("IIT/2023/001")
                    .build();

            // When
            Club club = Club.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .description("Technology club")
                    .logoUrl("https://s3.com/clubs/logos/tech.jpg")
                    .faculty(FacultyType.COMPUTING)
                    .email("tech@iit.edu.lk")
                    .contactNumber("0775555555")
                    .establishedDate(establishedDate)
                    .socialMediaLinks("https://fb.com/tech-club")
                    .president(president)
                    .maxMembers(200)
                    .isRegistrationOpen(true)
                    .isActive(true)
                    .build();

            // Then
            assertThat(club).isNotNull();
            assertThat(club.getId()).isEqualTo(1L);
            assertThat(club.getClubCode()).isEqualTo("TECH001");
            assertThat(club.getName()).isEqualTo("Tech Club");
            assertThat(club.getEmail()).isEqualTo("tech@iit.edu.lk");
            assertThat(club.getFaculty()).isEqualTo(FacultyType.COMPUTING);
            assertThat(club.getEstablishedDate()).isEqualTo(establishedDate);
            assertThat(club.getPresident()).isEqualTo(president);
            assertThat(club.getMaxMembers()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should create club with default values")
        void clubBuilder_withDefaults_success() {
            // When
            Club club = Club.builder()
                    .id(1L)
                    .clubCode("ARTS001")
                    .name("Arts Club")
                    .build();

            // Then
            assertThat(club).isNotNull();
            assertThat(club.getMaxMembers()).isEqualTo(500);
            assertThat(club.getIsRegistrationOpen()).isTrue();
            assertThat(club.getElections()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Club Validation")
    class ClubValidationTests {

        @Test
        @DisplayName("Should validate club code is not null")
        void clubCodeNotNull() {
            // When
            Club club = Club.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .build();

            // Then
            assertThat(club.getClubCode()).isNotNull();
            assertThat(club.getClubCode()).isNotEmpty();
        }

        @Test
        @DisplayName("Should validate club name is not null")
        void clubNameNotNull() {
            // When
            Club club = Club.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .build();

            // Then
            assertThat(club.getName()).isNotNull();
            assertThat(club.getName()).isNotEmpty();
        }
    }
}

