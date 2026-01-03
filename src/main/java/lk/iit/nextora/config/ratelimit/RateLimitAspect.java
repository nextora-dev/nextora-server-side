package lk.iit.nextora.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * AOP Aspect for annotation-based rate limiting.
 *
 * Usage:
 * <pre>
 * {@code @RateLimit(requests = 5, duration = 60, timeUnit = TimeUnit.SECONDS)}
 * public ResponseEntity<?> sensitiveEndpoint() { ... }
 * </pre>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties properties;

    @Around("@annotation(rateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        // Build key
        String key = buildKey(joinPoint, rateLimit);

        // Calculate duration
        Duration window = Duration.of(rateLimit.duration(), toChronoUnit(rateLimit.timeUnit()));

        // Check rate limit
        RateLimitResult result = rateLimiterService.checkRateLimit(key, rateLimit.requests(), window);

        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for method: {} with key: {}",
                    joinPoint.getSignature().getName(), key);
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Try again in " + result.getResetInSeconds() + " seconds.",
                    result
            );
        }

        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String prefix = rateLimit.keyPrefix().isEmpty()
                ? getMethodKey(joinPoint)
                : rateLimit.keyPrefix();

        String ip = extractClientIp();
        String userId = extractUserId();

        return rateLimiterService.buildKey(prefix, ip, userId, rateLimit.keyType());
    }

    private String getMethodKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    private String extractClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }

        HttpServletRequest request = attrs.getRequest();

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }

    private java.time.temporal.ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        return switch (timeUnit) {
            case NANOSECONDS -> java.time.temporal.ChronoUnit.NANOS;
            case MICROSECONDS -> java.time.temporal.ChronoUnit.MICROS;
            case MILLISECONDS -> java.time.temporal.ChronoUnit.MILLIS;
            case SECONDS -> java.time.temporal.ChronoUnit.SECONDS;
            case MINUTES -> java.time.temporal.ChronoUnit.MINUTES;
            case HOURS -> java.time.temporal.ChronoUnit.HOURS;
            case DAYS -> java.time.temporal.ChronoUnit.DAYS;
        };
    }

    /**
     * Custom exception for rate limit exceeded
     */
    public static class RateLimitExceededException extends RuntimeException {
        private final RateLimitResult result;

        public RateLimitExceededException(String message, RateLimitResult result) {
            super(message);
            this.result = result;
        }

        public RateLimitResult getResult() {
            return result;
        }
    }
}

