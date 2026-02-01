package lk.iit.nextora.infrastructure.notification.push.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK Configuration.
 *
 * Design Decisions:
 * - Service account key is externalized via configuration (never hardcoded)
 * - Supports both classpath and file system paths for flexibility
 * - Singleton FirebaseApp instance is initialized once at startup
 * - Graceful degradation if Firebase is not configured (logs warning instead of crashing)
 *
 * Security:
 * - Service account JSON should NEVER be committed to version control
 * - Use environment variables or external config files in production
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account.path:}")
    private String serviceAccountPath;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.warn("Firebase is disabled. Push notifications will not work.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccount = getServiceAccountStream();

                if (serviceAccount == null) {
                    log.error("Firebase service account file not found. Push notifications disabled.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");

            } catch (IOException e) {
                log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            }
        }
    }

    /**
     * Attempts to load the service account JSON from:
     * 1. File system path (if absolute path provided)
     * 2. Classpath resource (for bundled configs)
     */
    private InputStream getServiceAccountStream() throws IOException {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.warn("Firebase service account path not configured");
            return null;
        }

        // Try as file system path first
        if (serviceAccountPath.startsWith("/") || serviceAccountPath.contains(":")) {
            log.debug("Loading Firebase credentials from file system: {}", serviceAccountPath);
            return new FileInputStream(serviceAccountPath);
        }

        // Try as classpath resource
        log.debug("Loading Firebase credentials from classpath: {}", serviceAccountPath);
        Resource resource = new ClassPathResource(serviceAccountPath);
        if (resource.exists()) {
            return resource.getInputStream();
        }

        return null;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (!firebaseEnabled || FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseMessaging bean not created - Firebase is not initialized");
            return null;
        }
        return FirebaseMessaging.getInstance();
    }
}
