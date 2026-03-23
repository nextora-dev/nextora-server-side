package lk.iit.nextora.config.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Industry-level Cache Service for Redis operations.
 *
 * Features:
 * - Type-safe operations with generics
 * - Cache-aside pattern support
 * - Graceful degradation when Redis unavailable
 * - Key prefix management
 * - Bulk operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_SEPARATOR = ":";
    private static final String DEFAULT_PREFIX = "nextora";

    // ==================== Basic Operations ====================

    /**
     * Store value in cache with TTL
     */
    public <T> void put(String key, T value, Duration ttl) {
        try {
            String fullKey = buildKey(key);
            redisTemplate.opsForValue().set(fullKey, value, ttl);
            log.debug("Cache PUT: {} (TTL: {})", fullKey, ttl);
        } catch (Exception e) {
            log.warn("Failed to put cache key {}: {}", key, e.getMessage());
        }
    }

    /**
     * Store value in cache with default TTL (10 minutes)
     */
    public <T> void put(String key, T value) {
        put(key, value, Duration.ofMinutes(10));
    }

    /**
     * Get value from cache
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String fullKey = buildKey(key);
            Object value = redisTemplate.opsForValue().get(fullKey);
            if (value != null) {
                log.debug("Cache HIT: {}", fullKey);
                if (type.isInstance(value)) {
                    return Optional.of((T) value);
                }
                // Try JSON conversion
                return Optional.of(objectMapper.convertValue(value, type));
            }
            log.debug("Cache MISS: {}", fullKey);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to get cache key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get value or compute if absent (cache-aside pattern)
     */
    public <T> T getOrCompute(String key, Class<T> type, Supplier<T> supplier, Duration ttl) {
        Optional<T> cached = get(key, type);
        if (cached.isPresent()) {
            return cached.get();
        }

        T value = supplier.get();
        if (value != null) {
            put(key, value, ttl);
        }
        return value;
    }

    /**
     * Delete value from cache
     */
    public boolean delete(String key) {
        try {
            String fullKey = buildKey(key);
            Boolean result = redisTemplate.delete(fullKey);
            log.debug("Cache DELETE: {} (success: {})", fullKey, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("Failed to delete cache key {}: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        try {
            String fullKey = buildKey(key);
            return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
        } catch (Exception e) {
            log.warn("Failed to check cache key {}: {}", key, e.getMessage());
            return false;
        }
    }

    // ==================== Pattern-Based Operations ====================

    /**
     * Delete all keys matching pattern
     */
    public long deleteByPattern(String pattern) {
        try {
            String fullPattern = buildKey(pattern);
            Set<String> keys = redisTemplate.keys(fullPattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("Cache DELETE by pattern {}: {} keys deleted", fullPattern, deleted);
                return deleted != null ? deleted : 0;
            }
            return 0;
        } catch (Exception e) {
            log.warn("Failed to delete by pattern {}: {}", pattern, e.getMessage());
            return 0;
        }
    }

    /**
     * Get all keys matching pattern
     */
    public Set<String> getKeysByPattern(String pattern) {
        try {
            String fullPattern = buildKey(pattern);
            return redisTemplate.keys(fullPattern);
        } catch (Exception e) {
            log.warn("Failed to get keys by pattern {}: {}", pattern, e.getMessage());
            return Set.of();
        }
    }

    // ==================== User-Specific Operations ====================

    /**
     * Cache user profile
     */
    public <T> void cacheUserProfile(Long userId, T profile) {
        put("user:profile:" + userId, profile, Duration.ofMinutes(15));
    }

    /**
     * Get cached user profile
     */
    public <T> Optional<T> getCachedUserProfile(Long userId, Class<T> type) {
        return get("user:profile:" + userId, type);
    }

    /**
     * Evict user profile cache
     */
    public void evictUserProfile(Long userId) {
        delete("user:profile:" + userId);
    }

    /**
     * Evict all user-related caches
     */
    public void evictAllUserCaches(Long userId) {
        deleteByPattern("user:*:" + userId + "*");
    }

    /**
     * Cache user by email
     */
    public <T> void cacheUserByEmail(String email, T user) {
        put("user:email:" + email, user, Duration.ofMinutes(30));
    }

    /**
     * Get cached user by email
     */
    public <T> Optional<T> getCachedUserByEmail(String email, Class<T> type) {
        return get("user:email:" + email, type);
    }

    // ==================== List/Collection Operations ====================

    /**
     * Cache a list of items
     */
    public <T> void cacheList(String key, Collection<T> items, Duration ttl) {
        put(key, items, ttl);
    }

    /**
     * Cache users list
     */
    public <T> void cacheUsersList(Collection<T> users) {
        put("users:all", users, Duration.ofMinutes(5));
    }

    /**
     * Evict users list cache
     */
    public void evictUsersList() {
        delete("users:all");
    }

    // ==================== TTL Operations ====================

    /**
     * Get remaining TTL for a key
     */
    public Optional<Long> getTtl(String key) {
        try {
            String fullKey = buildKey(key);
            Long ttl = redisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? Optional.of(ttl) : Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to get TTL for key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extend TTL for a key
     */
    public boolean extendTtl(String key, Duration additionalTime) {
        try {
            String fullKey = buildKey(key);
            return Boolean.TRUE.equals(redisTemplate.expire(fullKey, additionalTime));
        } catch (Exception e) {
            log.warn("Failed to extend TTL for key {}: {}", key, e.getMessage());
            return false;
        }
    }

    // ==================== Counter Operations ====================

    /**
     * Increment counter
     */
    public Long increment(String key) {
        try {
            String fullKey = buildKey(key);
            return redisTemplate.opsForValue().increment(fullKey);
        } catch (Exception e) {
            log.warn("Failed to increment key {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Increment counter with expiry
     */
    public Long incrementWithExpiry(String key, Duration ttl) {
        try {
            String fullKey = buildKey(key);
            Long value = redisTemplate.opsForValue().increment(fullKey);
            if (value != null && value == 1) {
                redisTemplate.expire(fullKey, ttl);
            }
            return value;
        } catch (Exception e) {
            log.warn("Failed to increment key {}: {}", key, e.getMessage());
            return null;
        }
    }

    // ==================== Utility Methods ====================

    private String buildKey(String key) {
        if (key.startsWith(DEFAULT_PREFIX + KEY_SEPARATOR)) {
            return key;
        }
        return DEFAULT_PREFIX + KEY_SEPARATOR + key;
    }

    /**
     * Build cache key with multiple parts
     */
    public String buildCacheKey(String... parts) {
        return String.join(KEY_SEPARATOR, parts);
    }
}

