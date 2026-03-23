package lk.iit.nextora.config.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtBlacklistService Unit Tests")
class JwtBlacklistServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private ValueOperations<String, String> valueOperations;

    private JwtBlacklistService jwtBlacklistService;

    @BeforeEach
    void setUp() {
        jwtBlacklistService = new JwtBlacklistService(redisTemplate, jwtTokenProvider);
    }

    // ============================================================
    // BLACKLIST TOKEN TESTS
    // ============================================================

    @Nested
    @DisplayName("blacklistToken")
    class BlacklistTokenTests {

        @Test
        @DisplayName("Should blacklist token in Redis with correct TTL")
        void blacklistToken_validToken_storesInRedis() {
            // Given
            Date futureExpiry = new Date(System.currentTimeMillis() + 3600000); // +1 hour
            when(jwtTokenProvider.extractExpiration("test-token")).thenReturn(futureExpiry);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // When
            jwtBlacklistService.blacklistToken("test-token");

            // Then
            verify(valueOperations).set(eq("blacklist:test-token"), eq("LOGOUT"), any(Duration.class));
        }

        @Test
        @DisplayName("Should not blacklist already-expired token (TTL <= 0)")
        void blacklistToken_expiredToken_doesNothing() {
            // Given
            Date pastExpiry = new Date(System.currentTimeMillis() - 1000); // already expired
            when(jwtTokenProvider.extractExpiration("expired-token")).thenReturn(pastExpiry);

            // When
            jwtBlacklistService.blacklistToken("expired-token");

            // Then
            verifyNoInteractions(redisTemplate);
        }

        @Test
        @DisplayName("Should fallback to in-memory when Redis is unavailable")
        void blacklistToken_redisFailure_fallsBackToInMemory() {
            // Given
            Date futureExpiry = new Date(System.currentTimeMillis() + 3600000);
            when(jwtTokenProvider.extractExpiration("test-token")).thenReturn(futureExpiry);
            when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

            // When
            jwtBlacklistService.blacklistToken("test-token");

            // Then — verify by checking isBlacklisted returns true from in-memory
            when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis down"));
            boolean blacklisted = jwtBlacklistService.isBlacklisted("test-token");
            assertThat(blacklisted).isTrue();
        }
    }

    // ============================================================
    // IS BLACKLISTED TESTS
    // ============================================================

    @Nested
    @DisplayName("isBlacklisted")
    class IsBlacklistedTests {

        @Test
        @DisplayName("Should return true when token exists in Redis")
        void isBlacklisted_tokenInRedis_returnsTrue() {
            // Given
            when(redisTemplate.hasKey("blacklist:token-123")).thenReturn(true);

            // When
            boolean result = jwtBlacklistService.isBlacklisted("token-123");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when token not in Redis or in-memory")
        void isBlacklisted_tokenNotFound_returnsFalse() {
            // Given
            when(redisTemplate.hasKey("blacklist:unknown-token")).thenReturn(false);

            // When
            boolean result = jwtBlacklistService.isBlacklisted("unknown-token");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for in-memory entry that has expired")
        void isBlacklisted_inMemoryExpired_returnsFalse() {
            // Given — blacklist a token that expires in the past (simulate by Redis failure)
            Date pastExpiry = new Date(System.currentTimeMillis() + 1); // 1ms from now
            when(jwtTokenProvider.extractExpiration("soon-expired")).thenReturn(pastExpiry);
            when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

            jwtBlacklistService.blacklistToken("soon-expired");

            // Wait for it to expire
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis down"));

            // When
            boolean result = jwtBlacklistService.isBlacklisted("soon-expired");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should check in-memory when Redis throws exception")
        void isBlacklisted_redisException_checksInMemory() {
            // Given
            when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis down"));

            // When
            boolean result = jwtBlacklistService.isBlacklisted("any-token");

            // Then — no exception thrown, falls back gracefully
            assertThat(result).isFalse();
        }
    }
}
