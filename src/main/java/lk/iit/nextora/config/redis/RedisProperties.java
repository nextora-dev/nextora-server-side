package lk.iit.nextora.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis configuration properties bound to application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    private String host = "localhost";
    private int port = 6379;
    private String password = "";
    private Duration timeout = Duration.ofMillis(2000);

    private Lettuce lettuce = new Lettuce();

    @Data
    public static class Lettuce {
        private Pool pool = new Pool();

        @Data
        public static class Pool {
            private int maxActive = 8;
            private int maxIdle = 8;
            private int minIdle = 0;
            private Duration maxWait = Duration.ofMillis(-1);
        }
    }
}

