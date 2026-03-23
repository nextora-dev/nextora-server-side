package lk.iit.nextora.config.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method-level rate limiting.
 * Apply to controller methods to set custom rate limits.
 *
 * Example:
 * <pre>
 * {@code @RateLimit(requests = 10, duration = 60, timeUnit = TimeUnit.SECONDS)}
 * public ResponseEntity<?> sensitiveEndpoint() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum number of requests allowed in the time window
     */
    int requests() default 100;

    /**
     * Time window duration
     */
    int duration() default 60;

    /**
     * Time unit for duration (in seconds)
     */
    java.util.concurrent.TimeUnit timeUnit() default java.util.concurrent.TimeUnit.SECONDS;

    /**
     * Key type for rate limiting
     */
    KeyType keyType() default KeyType.IP;

    /**
     * Custom key prefix (optional)
     */
    String keyPrefix() default "";

    enum KeyType {
        /**
         * Rate limit by IP address
         */
        IP,
        /**
         * Rate limit by authenticated user
         */
        USER,
        /**
         * Rate limit by both IP and user
         */
        IP_AND_USER,
        /**
         * Rate limit globally (all requests combined)
         */
        GLOBAL
    }
}

