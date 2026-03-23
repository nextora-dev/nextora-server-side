package lk.iit.nextora.config.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.iit.nextora.common.exception.ErrorResponse;
import lk.iit.nextora.common.util.DateUtils;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

/**
 * Industry-level Rate Limit Filter.
 *
 * Features:
 * - Standard rate limit headers (X-RateLimit-*)
 * - Different limits for auth endpoints
 * - IP extraction with proxy support
 * - Proper JSON error responses
 * - Path exclusion patterns
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Standard rate limit headers
    private static final String HEADER_LIMIT = "X-RateLimit-Limit";
    private static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RESET = "X-RateLimit-Reset";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    public RateLimitFilter(RateLimiterService rateLimiterService,
                          RateLimitProperties properties,
                          ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip if rate limiting is disabled
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Check if path should be excluded
        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract client identifier
        String clientIp = extractClientIp(request);
        String userId = extractUserId();

        // Build rate limit key
        String key = buildRateLimitKey(path, clientIp, userId);

        // Check rate limit
        RateLimitResult result = rateLimiterService.checkRateLimitForPath(key, path);

        // Add rate limit headers
        if (properties.isIncludeHeaders()) {
            addRateLimitHeaders(response, result);
        }

        if (!result.isAllowed()) {
            handleRateLimitExceeded(request, response, result);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldExclude(String path) {
        return Arrays.stream(properties.getExcludePaths())
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * Extract client IP with proxy support (X-Forwarded-For, X-Real-IP)
     */
    private String extractClientIp(HttpServletRequest request) {
        // Check X-Forwarded-For header (common for proxies/load balancers)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            // Take the first IP (original client)
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header (Nginx)
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(xRealIp)) {
            return xRealIp;
        }

        // Check CF-Connecting-IP (Cloudflare)
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (StringUtils.isNotBlank(cfConnectingIp)) {
            return cfConnectingIp;
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }

    private String extractUserId() {
        return SecurityUtils.getCurrentUserEmail().orElse(null);
    }

    private String buildRateLimitKey(String path, String clientIp, String userId) {
        String prefix = path.replace("/", "_");

        // Use user ID if authenticated, otherwise IP
        if (StringUtils.isNotBlank(userId)) {
            return "ratelimit:" + prefix + ":user:" + userId;
        }
        return "ratelimit:" + prefix + ":ip:" + clientIp;
    }

    private void addRateLimitHeaders(HttpServletResponse response, RateLimitResult result) {
        response.setHeader(HEADER_LIMIT, String.valueOf(result.getLimit()));
        response.setHeader(HEADER_REMAINING, String.valueOf(result.getRemaining()));
        response.setHeader(HEADER_RESET, String.valueOf(Instant.now().plusSeconds(result.getResetInSeconds()).getEpochSecond()));
    }

    private void handleRateLimitExceeded(HttpServletRequest request,
                                         HttpServletResponse response,
                                         RateLimitResult result) throws IOException {
        log.warn("Rate limit exceeded for key: {} on path: {}", result.getKey(), request.getRequestURI());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HEADER_RETRY_AFTER, String.valueOf(result.getResetInSeconds()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(DateUtils.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message("Rate limit exceeded. Please try again in " + result.getResetInSeconds() + " seconds.")
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
