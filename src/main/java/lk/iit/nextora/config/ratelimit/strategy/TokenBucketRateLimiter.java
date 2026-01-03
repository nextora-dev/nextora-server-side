package lk.iit.nextora.config.ratelimit.strategy;

import lk.iit.nextora.config.ratelimit.RateLimitResult;
import lk.iit.nextora.config.ratelimit.RateLimitStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token Bucket Rate Limiter implementation.
 * Allows burst traffic while maintaining average rate.
 * Tokens are added at a fixed rate and consumed per request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimitStrategy {

    private final RedisTemplate<String, String> redisTemplate;

    // In-memory fallback
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequests, Duration window) {
        try {
            if (redisTemplate != null) {
                return tryAcquireRedis(key, maxRequests, window);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to in-memory: {}", e.getMessage());
        }
        return tryAcquireInMemory(key, maxRequests, window);
    }

    private RateLimitResult tryAcquireRedis(String key, int maxRequests, Duration window) {
        String tokensKey = key + ":tokens";
        String timestampKey = key + ":ts";

        long now = System.currentTimeMillis();
        double refillRate = (double) maxRequests / window.toMillis(); // tokens per millisecond

        try {
            // Get current state
            String tokensStr = redisTemplate.opsForValue().get(tokensKey);
            String tsStr = redisTemplate.opsForValue().get(timestampKey);

            double tokens = tokensStr != null ? Double.parseDouble(tokensStr) : maxRequests;
            long lastRefill = tsStr != null ? Long.parseLong(tsStr) : now;

            // Calculate tokens to add
            long elapsed = now - lastRefill;
            tokens = Math.min(maxRequests, tokens + elapsed * refillRate);

            if (tokens >= 1) {
                // Consume token
                tokens -= 1;
                redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens));
                redisTemplate.opsForValue().set(timestampKey, String.valueOf(now));
                redisTemplate.expire(tokensKey, window.multipliedBy(2));
                redisTemplate.expire(timestampKey, window.multipliedBy(2));

                long resetSeconds = tokens < 1 ? (long) ((1 - tokens) / refillRate / 1000) : 0;
                return RateLimitResult.allowed(maxRequests, (int) tokens, resetSeconds, key);
            } else {
                // Calculate time until next token
                long waitTime = (long) ((1 - tokens) / refillRate);
                return RateLimitResult.denied(maxRequests, waitTime / 1000, key);
            }
        } catch (Exception e) {
            log.warn("Redis error: {}", e.getMessage());
            return RateLimitResult.allowed(maxRequests, maxRequests - 1, window.toSeconds(), key);
        }
    }

    private RateLimitResult tryAcquireInMemory(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        double refillRate = (double) maxRequests / window.toMillis();

        TokenBucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null) {
                return new TokenBucket(maxRequests - 1, now);
            }

            // Refill tokens
            long elapsed = now - existing.lastRefillTimestamp;
            double newTokens = Math.min(maxRequests, existing.tokens + elapsed * refillRate);

            if (newTokens >= 1) {
                existing.tokens = newTokens - 1;
                existing.lastRefillTimestamp = now;
                existing.allowed = true;
            } else {
                existing.tokens = newTokens;
                existing.allowed = false;
            }

            return existing;
        });

        if (bucket.allowed) {
            long resetSeconds = bucket.tokens < 1 ? (long) ((1 - bucket.tokens) / refillRate / 1000) : 0;
            return RateLimitResult.allowed(maxRequests, (int) bucket.tokens, resetSeconds, key);
        } else {
            long waitTime = (long) ((1 - bucket.tokens) / refillRate / 1000);
            return RateLimitResult.denied(maxRequests, waitTime, key);
        }
    }

    @Override
    public int getRemainingRequests(String key, int maxRequests, Duration window) {
        TokenBucket bucket = buckets.get(key);
        if (bucket == null) {
            return maxRequests;
        }

        long now = System.currentTimeMillis();
        double refillRate = (double) maxRequests / window.toMillis();
        long elapsed = now - bucket.lastRefillTimestamp;
        double tokens = Math.min(maxRequests, bucket.tokens + elapsed * refillRate);

        return (int) tokens;
    }

    @Override
    public void reset(String key) {
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(key + ":tokens");
                redisTemplate.delete(key + ":ts");
            }
        } catch (Exception e) {
            log.warn("Failed to reset Redis keys: {}", e.getMessage());
        }
        buckets.remove(key);
    }

    private static class TokenBucket {
        double tokens;
        long lastRefillTimestamp;
        boolean allowed = true;

        TokenBucket(double tokens, long timestamp) {
            this.tokens = tokens;
            this.lastRefillTimestamp = timestamp;
        }
    }
}

