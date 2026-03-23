package lk.iit.nextora.config.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for cache eviction with pattern support.
 *
 * Example:
 * <pre>
 * {@code @CacheEvictPattern(patterns = {"user:*:123", "users:all"})}
 * public void updateUser(Long userId) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvictPattern {

    /**
     * Cache key patterns to evict (supports wildcards)
     */
    String[] patterns() default {};

    /**
     * Whether to evict before or after method execution
     */
    boolean beforeInvocation() default false;
}

