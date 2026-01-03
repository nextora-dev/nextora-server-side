package lk.iit.nextora.config.ratelimit;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate limiting configuration properties.
 * Configurable via application.yml
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /**
     * Enable or disable rate limiting globally
     */
    private boolean enabled = true;

    /**
     * Default rate limit for anonymous users
     */
    private LimitConfig anonymous = new LimitConfig(60, Duration.ofMinutes(1));

    /**
     * Default rate limit for authenticated users
     */
    private LimitConfig authenticated = new LimitConfig(200, Duration.ofMinutes(1));

    /**
     * Rate limit for authentication endpoints (login, register)
     */
    private LimitConfig auth = new LimitConfig(10, Duration.ofMinutes(1));

    /**
     * Rate limit for sensitive endpoints (password reset, etc.)
     */
    private LimitConfig sensitive = new LimitConfig(5, Duration.ofMinutes(5));

    /**
     * Endpoint-specific overrides
     * Key: endpoint path pattern, Value: limit configuration
     */
    private Map<String, LimitConfig> endpoints = new HashMap<>();

    /**
     * Paths to exclude from rate limiting
     */
    private String[] excludePaths = {
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    /**
     * Whether to include rate limit headers in response
     */
    private boolean includeHeaders = true;

    /**
     * Algorithm to use: FIXED_WINDOW, SLIDING_WINDOW, TOKEN_BUCKET
     */
    private Algorithm algorithm = Algorithm.SLIDING_WINDOW;

    @PostConstruct
    public void logConfig() {
        log.info("Rate Limit Configuration loaded:");
        log.info("  Enabled: {}", enabled);
        log.info("  Algorithm: {}", algorithm);
        log.info("  Anonymous limit: {} requests per {}", anonymous.getMaxRequests(), anonymous.getWindow());
        log.info("  Authenticated limit: {} requests per {}", authenticated.getMaxRequests(), authenticated.getWindow());
        log.info("  Auth endpoints limit: {} requests per {}", auth.getMaxRequests(), auth.getWindow());
        log.info("  Endpoint-specific configs from YAML: {}", endpoints.size());

        // If endpoints is empty, add defaults programmatically
        if (endpoints.isEmpty()) {
            log.warn("No endpoint configs loaded from YAML, adding defaults programmatically");
            endpoints.put("/api/v1/auth/login", new LimitConfig(5, Duration.ofMinutes(1)));
            endpoints.put("/api/v1/auth/register", new LimitConfig(3, Duration.ofMinutes(1)));
            endpoints.put("/api/v1/users/**", new LimitConfig(2, Duration.ofMinutes(1)));
        }

        log.info("  Total endpoint-specific configs: {}", endpoints.size());
        endpoints.forEach((path, config) ->
            log.info("    {} -> {} requests per {}", path, config.getMaxRequests(), config.getWindow())
        );
    }

    @Data
    public static class LimitConfig {
        private int maxRequests;
        private Duration window;

        public LimitConfig() {
            this.maxRequests = 100;
            this.window = Duration.ofMinutes(1);
        }

        public LimitConfig(int maxRequests, Duration window) {
            this.maxRequests = maxRequests;
            this.window = window;
        }
    }

    public enum Algorithm {
        FIXED_WINDOW,
        SLIDING_WINDOW,
        TOKEN_BUCKET
    }
}

