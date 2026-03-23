package lk.iit.nextora.config.ratelimit;

import java.time.Duration;

/**
 * Interface for rate limiting strategies.
 * Allows different algorithms to be used interchangeably.
 */
public interface RateLimitStrategy {

    /**
     * Check if a request is allowed and record it
     *
     * @param key         the rate limit key (e.g., IP address, user ID)
     * @param maxRequests maximum requests allowed in the window
     * @param window      time window duration
     * @return RateLimitResult containing limit information
     */
    RateLimitResult tryAcquire(String key, int maxRequests, Duration window);

    /**
     * Get remaining requests without consuming
     *
     * @param key         the rate limit key
     * @param maxRequests maximum requests allowed
     * @param window      time window duration
     * @return remaining requests count
     */
    int getRemainingRequests(String key, int maxRequests, Duration window);

    /**
     * Reset rate limit for a specific key
     *
     * @param key the rate limit key to reset
     */
    void reset(String key);
}

