package lk.iit.nextora.config.redis;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);


    public boolean isAllowed(String key) {
        // If redisTemplate is not available (tests may subclass and pass null) or Redis is down,
        // fail open: allow requests instead of throwing exceptions and causing 5xx errors.
        if (redisTemplate == null) {
            log.debug("RedisTemplate is null in RateLimiterService; skipping rate limiting for key={}", key);
            return true;
        }

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count == null) {
                // unexpected, but treat as allowed
                log.warn("Redis returned null increment for key={}; treating as allowed", key);
                return true;
            }

            if (count == 1) {
                try {
                    redisTemplate.expire(key, WINDOW);
                } catch (Exception ex) {
                    // expire failing is non-fatal for rate limiting; log and continue
                    log.warn("Failed to set expire on Redis key={}: {}", key, ex.getMessage());
                }
            }

            return count <= MAX_REQUESTS;
        } catch (Exception ex) {
            // Common reasons: RedisConnectionFailureException, Lettuce exceptions, etc.
            // We don't want the whole request to fail because Redis is down — degrade gracefully.
            log.warn("Redis unavailable for rate limiting (key={}), allowing request: {}", key, ex.toString());
            return true;
        }
    }
}
