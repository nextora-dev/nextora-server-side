package lk.iit.nextora.config.ratelimit;

import lk.iit.nextora.config.ratelimit.strategy.SlidingWindowRateLimiter;
import lk.iit.nextora.config.ratelimit.strategy.TokenBucketRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Industry-level Rate Limiting Service.
 *
 * Features:
 * - Multiple rate limiting algorithms (Sliding Window, Token Bucket)
 * - Different limits for authenticated vs anonymous users
 * - Endpoint-specific rate limits
 * - Redis-backed with in-memory fallback
 * - Configurable via application.yml
 */
@Slf4j
@Service
public class RateLimiterService {

    private final RateLimitProperties properties;
    private final SlidingWindowRateLimiter slidingWindowRateLimiter;
    private final TokenBucketRateLimiter tokenBucketRateLimiter;

    public RateLimiterService(
            RateLimitProperties properties,
            SlidingWindowRateLimiter slidingWindowRateLimiter,
            TokenBucketRateLimiter tokenBucketRateLimiter) {
        this.properties = properties;
        this.slidingWindowRateLimiter = slidingWindowRateLimiter;
        this.tokenBucketRateLimiter = tokenBucketRateLimiter;
    }

    /**
     * Check if request is allowed based on IP
     * (Backward compatible method)
     */
    public boolean isAllowed(String key) {
        if (!properties.isEnabled()) {
            return true;
        }

        RateLimitProperties.LimitConfig config = isAuthenticated()
                ? properties.getAuthenticated()
                : properties.getAnonymous();

        RateLimitResult result = checkRateLimit(key, config.getMaxRequests(), config.getWindow());
        return result.isAllowed();
    }

    /**
     * Check rate limit and return detailed result
     */
    public RateLimitResult checkRateLimit(String key, int maxRequests, Duration window) {
        if (!properties.isEnabled()) {
            return RateLimitResult.allowed(maxRequests, maxRequests, window.toSeconds(), key);
        }

        RateLimitStrategy strategy = getStrategy();
        return strategy.tryAcquire(key, maxRequests, window);
    }

    /**
     * Check rate limit for a specific endpoint path
     */
    public RateLimitResult checkRateLimitForPath(String key, String path) {
        if (!properties.isEnabled()) {
            return RateLimitResult.allowed(100, 100, 60, key);
        }

        // Check for endpoint-specific config
        RateLimitProperties.LimitConfig config = findConfigForPath(path);
        return checkRateLimit(key, config.getMaxRequests(), config.getWindow());
    }

    /**
     * Check rate limit for authentication endpoints
     */
    public RateLimitResult checkAuthRateLimit(String key) {
        RateLimitProperties.LimitConfig config = properties.getAuth();
        return checkRateLimit(key, config.getMaxRequests(), config.getWindow());
    }

    /**
     * Check rate limit for sensitive endpoints
     */
    public RateLimitResult checkSensitiveRateLimit(String key) {
        RateLimitProperties.LimitConfig config = properties.getSensitive();
        return checkRateLimit(key, config.getMaxRequests(), config.getWindow());
    }

    /**
     * Get remaining requests for a key
     */
    public int getRemainingRequests(String key) {
        RateLimitProperties.LimitConfig config = isAuthenticated()
                ? properties.getAuthenticated()
                : properties.getAnonymous();

        return getStrategy().getRemainingRequests(key, config.getMaxRequests(), config.getWindow());
    }

    /**
     * Reset rate limit for a key (useful for testing or admin operations)
     */
    public void resetRateLimit(String key) {
        getStrategy().reset(key);
        log.info("Rate limit reset for key: {}", key);
    }

    /**
     * Build rate limit key based on type
     */
    public String buildKey(String prefix, String ip, String userId, RateLimit.KeyType keyType) {
        return switch (keyType) {
            case IP -> "ratelimit:" + prefix + ":ip:" + ip;
            case USER -> "ratelimit:" + prefix + ":user:" + (userId != null ? userId : ip);
            case IP_AND_USER -> "ratelimit:" + prefix + ":ip:" + ip + ":user:" + (userId != null ? userId : "anon");
            case GLOBAL -> "ratelimit:" + prefix + ":global";
        };
    }

    /**
     * Build default key for IP-based rate limiting
     */
    public String buildDefaultKey(String ip) {
        return "ratelimit:default:ip:" + ip;
    }

    private RateLimitStrategy getStrategy() {
        return switch (properties.getAlgorithm()) {
            case TOKEN_BUCKET -> tokenBucketRateLimiter;
            case SLIDING_WINDOW, FIXED_WINDOW -> slidingWindowRateLimiter;
        };
    }

    private RateLimitProperties.LimitConfig findConfigForPath(String path) {
        log.debug("Finding rate limit config for path: {}", path);

        // Check endpoint-specific overrides
        for (var entry : properties.getEndpoints().entrySet()) {
            log.debug("Checking pattern: {} against path: {}", entry.getKey(), path);
            if (pathMatches(path, entry.getKey())) {
                log.debug("MATCH! Using config: {} requests per {}",
                    entry.getValue().getMaxRequests(), entry.getValue().getWindow());
                return entry.getValue();
            }
        }

        // Check if it's an auth endpoint
        if (path.contains("/auth/")) {
            log.debug("Auth endpoint detected, using auth config");
            return properties.getAuth();
        }

        // Return default based on authentication status
        boolean authenticated = isAuthenticated();
        log.debug("No specific config found, using {} config", authenticated ? "authenticated" : "anonymous");
        return authenticated ? properties.getAuthenticated() : properties.getAnonymous();
    }

    private boolean pathMatches(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern) || path.matches(pattern.replace("*", ".*"));
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
}
