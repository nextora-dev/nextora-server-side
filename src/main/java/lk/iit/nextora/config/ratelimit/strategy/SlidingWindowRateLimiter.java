package lk.iit.nextora.config.ratelimit.strategy;

import lk.iit.nextora.config.ratelimit.RateLimitResult;
import lk.iit.nextora.config.ratelimit.RateLimitStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding Window Rate Limiter implementation.
 * Uses Redis sorted sets for distributed rate limiting with sub-second precision.
 * Falls back to in-memory implementation when Redis is unavailable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlidingWindowRateLimiter implements RateLimitStrategy {

    private final RedisTemplate<String, String> redisTemplate;

    // In-memory fallback storage
    private final Map<String, WindowData> inMemoryStore = new ConcurrentHashMap<>();

    // Lua script for atomic sliding window operation in Redis
    private static final String SLIDING_WINDOW_SCRIPT = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local clearBefore = now - window
            
            -- Remove old entries outside the window
            redis.call('ZREMRANGEBYSCORE', key, '-inf', clearBefore)
            
            -- Count current requests in window
            local count = redis.call('ZCARD', key)
            
            if count < limit then
                -- Add new request with current timestamp as score
                redis.call('ZADD', key, now, now .. '-' .. math.random())
                redis.call('PEXPIRE', key, window)
                return {1, limit - count - 1, window}
            else
                -- Get TTL for reset time
                local ttl = redis.call('PTTL', key)
                return {0, 0, ttl > 0 and ttl or window}
            end
            """;

    private final DefaultRedisScript<List> slidingWindowScript = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, List.class);

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequests, Duration window) {
        try {
            if (redisTemplate != null) {
                return tryAcquireRedis(key, maxRequests, window);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for rate limiting, falling back to in-memory: {}", e.getMessage());
        }
        return tryAcquireInMemory(key, maxRequests, window);
    }

    private RateLimitResult tryAcquireRedis(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();

        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(
                slidingWindowScript,
                Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(windowMillis),
                String.valueOf(maxRequests)
        );

        if (result == null || result.isEmpty()) {
            log.warn("Redis script returned null, allowing request");
            return RateLimitResult.allowed(maxRequests, maxRequests - 1, window.toSeconds(), key);
        }

        boolean allowed = result.get(0) == 1L;
        int remaining = result.get(1).intValue();
        long resetMs = result.get(2);

        if (allowed) {
            return RateLimitResult.allowed(maxRequests, remaining, resetMs / 1000, key);
        } else {
            return RateLimitResult.denied(maxRequests, resetMs / 1000, key);
        }
    }

    private RateLimitResult tryAcquireInMemory(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();

        WindowData data = inMemoryStore.compute(key, (k, existing) -> {
            if (existing == null) {
                return new WindowData(now, 1);
            }

            // Slide the window
            long windowStart = now - windowMillis;
            if (existing.windowStart < windowStart) {
                // Reset window
                return new WindowData(now, 1);
            }

            // Increment count
            existing.count++;
            return existing;
        });

        long resetInSeconds = (data.windowStart + windowMillis - now) / 1000;

        if (data.count <= maxRequests) {
            return RateLimitResult.allowed(maxRequests, maxRequests - (int) data.count, resetInSeconds, key);
        } else {
            return RateLimitResult.denied(maxRequests, resetInSeconds, key);
        }
    }

    @Override
    public int getRemainingRequests(String key, int maxRequests, Duration window) {
        try {
            if (redisTemplate != null) {
                long now = System.currentTimeMillis();
                long clearBefore = now - window.toMillis();

                // Remove old entries and count
                redisTemplate.opsForZSet().removeRangeByScore(key, Double.NEGATIVE_INFINITY, clearBefore);
                Long count = redisTemplate.opsForZSet().zCard(key);

                return maxRequests - (count != null ? count.intValue() : 0);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable: {}", e.getMessage());
        }

        WindowData data = inMemoryStore.get(key);
        if (data == null) {
            return maxRequests;
        }
        return Math.max(0, maxRequests - (int) data.count);
    }

    @Override
    public void reset(String key) {
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.warn("Failed to reset Redis key: {}", e.getMessage());
        }
        inMemoryStore.remove(key);
    }

    /**
     * Clean up expired in-memory entries periodically
     */
    public void cleanupExpiredEntries(Duration maxAge) {
        long threshold = System.currentTimeMillis() - maxAge.toMillis();
        inMemoryStore.entrySet().removeIf(entry -> entry.getValue().windowStart < threshold);
    }

    private static class WindowData {
        long windowStart;
        long count;

        WindowData(long windowStart, long count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

