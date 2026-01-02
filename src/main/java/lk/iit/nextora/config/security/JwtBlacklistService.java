package lk.iit.nextora.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, Long> inMemoryBlacklist = new ConcurrentHashMap<>();
    private static final String PREFIX = "blacklist:";

    public void blacklistToken(String token) {
        Date expiration = jwtTokenProvider.extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl <= 0) return;

        String key = PREFIX + token;
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, "LOGOUT", Duration.ofMillis(ttl));
                inMemoryBlacklist.remove(key);
                return;
            }
        } catch (Exception e) {
            // fallback to in-memory
        }
        inMemoryBlacklist.put(key, System.currentTimeMillis() + ttl);
    }

    public boolean isBlacklisted(String token) {
        String key = PREFIX + token;
        try {
            if (redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(key))) return true;
        } catch (Exception ignored) {}
        Long expiry = inMemoryBlacklist.get(key);
        if (expiry == null) return false;
        if (expiry < System.currentTimeMillis()) { inMemoryBlacklist.remove(key); return false; }
        return true;
    }
}
