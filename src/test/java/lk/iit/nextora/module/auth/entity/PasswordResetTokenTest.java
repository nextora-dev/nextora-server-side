package lk.iit.nextora.module.auth.entity;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordResetToken Entity Unit Tests")
class PasswordResetTokenTest {

    private Student testUser;

    @BeforeEach
    void setUp() {
        testUser = new Student();
        testUser.setId(1L);
        testUser.setEmail("user@iit.ac.lk");
        testUser.setPassword("encoded");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.ROLE_STUDENT);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Nested
    @DisplayName("Token Construction")
    class ConstructionTests {

        @Test
        @DisplayName("Should generate UUID token on construction")
        void constructor_validUser_generatesUUID() {
            PasswordResetToken token = new PasswordResetToken(testUser);

            assertThat(token.getToken()).isNotNull().isNotBlank();
            assertThat(token.getToken()).hasSize(36); // UUID format
        }

        @Test
        @DisplayName("Should set correct user reference")
        void constructor_validUser_setsUser() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            assertThat(token.getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Should set createdAt to current time")
        void constructor_validUser_setsCreatedAt() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            PasswordResetToken token = new PasswordResetToken(testUser);
            assertThat(token.getCreatedAt()).isAfter(before);
        }

        @Test
        @DisplayName("Should set expiry 60 minutes in the future")
        void constructor_validUser_setsExpiryTo60Minutes() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now().plusMinutes(59));
            assertThat(token.getExpiryDate()).isBefore(LocalDateTime.now().plusMinutes(61));
        }

        @Test
        @DisplayName("Should initialize used as false")
        void constructor_validUser_usedIsFalse() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            assertThat(token.isUsed()).isFalse();
        }

        @Test
        @DisplayName("Each token should have a unique UUID")
        void constructor_multipleCalls_uniqueTokens() {
            PasswordResetToken token1 = new PasswordResetToken(testUser);
            PasswordResetToken token2 = new PasswordResetToken(testUser);
            assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
        }
    }

    @Nested
    @DisplayName("Token Validity Checks")
    class ValidityTests {

        @Test
        @DisplayName("Fresh token should not be expired")
        void isExpired_freshToken_returnsFalse() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Token with past expiry should be expired")
        void isExpired_pastExpiry_returnsTrue() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            assertThat(token.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Fresh unused token should be valid")
        void isValid_freshUnusedToken_returnsTrue() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            assertThat(token.isValid()).isTrue();
        }

        @Test
        @DisplayName("Used token should not be valid")
        void isValid_usedToken_returnsFalse() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            token.markAsUsed();
            assertThat(token.isValid()).isFalse();
        }

        @Test
        @DisplayName("Expired token should not be valid")
        void isValid_expiredToken_returnsFalse() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            assertThat(token.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsUsed")
    class MarkAsUsedTests {

        @Test
        @DisplayName("Should set used flag to true")
        void markAsUsed_setsUsedTrue() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            token.markAsUsed();
            assertThat(token.isUsed()).isTrue();
        }

        @Test
        @DisplayName("Should set usedAt timestamp")
        void markAsUsed_setsUsedAtTimestamp() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            token.markAsUsed();
            assertThat(token.getUsedAt()).isNotNull();
            assertThat(token.getUsedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("getRemainingMinutes")
    class RemainingMinutesTests {

        @Test
        @DisplayName("Should return positive minutes for non-expired token")
        void getRemainingMinutes_freshToken_returnsPositive() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            assertThat(token.getRemainingMinutes()).isGreaterThan(0);
            assertThat(token.getRemainingMinutes()).isLessThanOrEqualTo(60);
        }

        @Test
        @DisplayName("Should return 0 for expired token")
        void getRemainingMinutes_expiredToken_returnsZero() {
            PasswordResetToken token = new PasswordResetToken(testUser);
            token.setExpiryDate(LocalDateTime.now().minusMinutes(5));
            assertThat(token.getRemainingMinutes()).isZero();
        }
    }
}
