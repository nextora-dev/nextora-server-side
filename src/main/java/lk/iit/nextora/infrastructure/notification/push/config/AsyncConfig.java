package lk.iit.nextora.infrastructure.notification.push.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for Push Notifications.
 *
 * Enables:
 * - @Async methods for non-blocking notification sending
 * - @Scheduled methods for token cleanup
 *
 * Design:
 * - Custom thread pool for push notification tasks
 * - Configurable pool size based on expected load
 * - Named threads for easier debugging in logs
 */
@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class AsyncConfig {

    /**
     * Custom executor for push notification tasks.
     * Separates notification sending from the main request thread pool.
     *
     * Pool sizing guidelines:
     * - Core size: Number of concurrent notification batches
     * - Max size: Handle bursts (e.g., broadcast notifications)
     * - Queue: Buffer for pending tasks
     */
    @Bean(name = "pushNotificationExecutor")
    public Executor pushNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("push-notif-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("Push notification executor initialized with core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), 100);

        return executor;
    }
}
