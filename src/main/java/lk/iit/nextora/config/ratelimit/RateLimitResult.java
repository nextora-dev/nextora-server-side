package lk.iit.nextora.config.ratelimit;

import lombok.Builder;
import lombok.Data;

/**
 * Result of a rate limit check containing limit information
 */
@Data
@Builder
public class RateLimitResult {

    /**
     * Whether the request is allowed
     */
    private boolean allowed;

    /**
     * Maximum requests allowed in the window
     */
    private int limit;

    /**
     * Remaining requests in current window
     */
    private int remaining;

    /**
     * Seconds until the rate limit resets
     */
    private long resetInSeconds;

    /**
     * Number of requests made in current window
     */
    private long currentCount;

    /**
     * The key used for rate limiting
     */
    private String key;

    public static RateLimitResult allowed(int limit, int remaining, long resetInSeconds, String key) {
        return RateLimitResult.builder()
                .allowed(true)
                .limit(limit)
                .remaining(Math.max(0, remaining))
                .resetInSeconds(resetInSeconds)
                .currentCount(limit - remaining)
                .key(key)
                .build();
    }

    public static RateLimitResult denied(int limit, long resetInSeconds, String key) {
        return RateLimitResult.builder()
                .allowed(false)
                .limit(limit)
                .remaining(0)
                .resetInSeconds(resetInSeconds)
                .currentCount(limit)
                .key(key)
                .build();
    }
}

