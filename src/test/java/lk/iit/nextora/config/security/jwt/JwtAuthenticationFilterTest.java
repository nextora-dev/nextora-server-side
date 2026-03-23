package lk.iit.nextora.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserDetailsService userDetailsService;
    @Mock private JwtBlacklistService jwtBlacklistService;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Student testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        response = new MockHttpServletResponse();

        testUser = new Student();
        testUser.setId(1L);
        testUser.setEmail("user@iit.ac.lk");
        testUser.setPassword("encoded");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.ROLE_STUDENT);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    // ============================================================
    // HAPPY PATH TESTS
    // ============================================================

    @Nested
    @DisplayName("Successful Authentication")
    class SuccessfulAuthTests {

        @Test
        @DisplayName("Should authenticate user with valid Bearer token")
        void doFilter_validBearerToken_setsSecurityContext() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Bearer valid-jwt-token");
            when(jwtBlacklistService.isBlacklisted("valid-jwt-token")).thenReturn(false);
            when(jwtTokenProvider.extractUsername("valid-jwt-token")).thenReturn("user@iit.ac.lk");
            when(userDetailsService.loadUserByUsername("user@iit.ac.lk")).thenReturn(testUser);
            when(jwtTokenProvider.isTokenValid("valid-jwt-token", testUser)).thenReturn(true);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@iit.ac.lk");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain without auth when no Authorization header")
        void doFilter_noAuthHeader_continuesWithoutAuth() throws ServletException, IOException {
            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain when Authorization header is not Bearer")
        void doFilter_nonBearerAuth_continuesWithoutAuth() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }
    }

    // ============================================================
    // BLACKLISTED TOKEN TESTS
    // ============================================================

    @Nested
    @DisplayName("Blacklisted Token Handling")
    class BlacklistedTokenTests {

        @Test
        @DisplayName("Should return 401 for blacklisted token")
        void doFilter_blacklistedToken_returns401() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Bearer blacklisted-token");
            when(jwtBlacklistService.isBlacklisted("blacklisted-token")).thenReturn(true);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("invalidated");
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    // ============================================================
    // INVALID TOKEN TESTS
    // ============================================================

    @Nested
    @DisplayName("Invalid Token Handling")
    class InvalidTokenTests {

        @Test
        @DisplayName("Should return 401 for invalid/non-validatable token")
        void doFilter_invalidToken_returns401() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Bearer invalid-token");
            when(jwtBlacklistService.isBlacklisted("invalid-token")).thenReturn(false);
            when(jwtTokenProvider.extractUsername("invalid-token")).thenReturn("user@iit.ac.lk");
            when(userDetailsService.loadUserByUsername("user@iit.ac.lk")).thenReturn(testUser);
            when(jwtTokenProvider.isTokenValid("invalid-token", testUser)).thenReturn(false);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("Invalid or expired token");
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should return 401 for expired JWT token")
        void doFilter_expiredToken_returns401() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Bearer expired-token");
            when(jwtBlacklistService.isBlacklisted("expired-token")).thenReturn(false);
            when(jwtTokenProvider.extractUsername("expired-token"))
                    .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("expired");
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should return 401 for malformed JWT token")
        void doFilter_malformedToken_returns401() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Bearer malformed-token");
            when(jwtBlacklistService.isBlacklisted("malformed-token")).thenReturn(false);
            when(jwtTokenProvider.extractUsername("malformed-token"))
                    .thenThrow(new io.jsonwebtoken.MalformedJwtException("Malformed"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("Malformed");
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should return 401 for token with invalid signature")
        void doFilter_badSignatureToken_returns401() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Bearer bad-sig-token");
            when(jwtBlacklistService.isBlacklisted("bad-sig-token")).thenReturn(false);
            when(jwtTokenProvider.extractUsername("bad-sig-token"))
                    .thenThrow(new io.jsonwebtoken.security.SignatureException("Invalid signature"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("Invalid token signature");
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should return 401 for generic exception during token parsing")
        void doFilter_genericException_returns401() throws ServletException, IOException {
            // Given
            request.addHeader("Authorization", "Bearer error-token");
            when(jwtBlacklistService.isBlacklisted("error-token")).thenReturn(false);
            when(jwtTokenProvider.extractUsername("error-token"))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(401);
            verify(filterChain, never()).doFilter(request, response);
        }
    }

    // ============================================================
    // SECURITY CONTEXT TESTS
    // ============================================================

    @Nested
    @DisplayName("Security Context Handling")
    class SecurityContextTests {

        @Test
        @DisplayName("Should not override existing authentication in SecurityContext")
        void doFilter_existingAuth_doesNotOverride() throws ServletException, IOException {
            // Given — pre-set authentication
            var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "existing@iit.ac.lk", null, java.util.Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            request.addHeader("Authorization", "Bearer valid-token");
            when(jwtBlacklistService.isBlacklisted("valid-token")).thenReturn(false);
            when(jwtTokenProvider.extractUsername("valid-token")).thenReturn("user@iit.ac.lk");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then — original auth preserved
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("existing@iit.ac.lk");
            verify(filterChain).doFilter(request, response);
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }
    }
}
