package lk.iit.nextora.infrastructure.notification.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.entity.FcmToken;
import lk.iit.nextora.infrastructure.notification.push.repository.FcmTokenRepository;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Controller for Kuppi Notifications (DEV ONLY).
 *
 * This controller allows testing push notifications without a frontend.
 * It provides endpoints to:
 * - Register a fake FCM token for testing
 * - Simulate Kuppi session creation notification
 * - Check notification status
 *
 * ⚠️ ONLY AVAILABLE IN DEV PROFILE - DO NOT USE IN PRODUCTION
 */
@RestController
@RequestMapping("/api/v1/test/kuppi-notifications")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
@Tag(name = "Kuppi Notifications - Testing", description = "DEV ONLY: Test Kuppi push notifications without frontend")
@SecurityRequirement(name = "bearerAuth")
public class KuppiNotificationTestController {

    private final KuppiNotificationService kuppiNotificationService;
    private final PushNotificationService pushNotificationService;
    private final FcmTokenRepository fcmTokenRepository;

    /**
     * Register a test FCM token for the current user.
     * This simulates what the frontend would do.
     */
    @PostMapping("/register-test-token")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Register a test FCM token",
            description = "Register a fake FCM token for testing. This simulates frontend token registration."
    )
    public ApiResponse<Map<String, Object>> registerTestToken(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        String testToken = "test-fcm-token-" + userDetails.getId() + "-" + System.currentTimeMillis();

        // Check if user already has a test token
        List<FcmToken> existingTokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userDetails.getId());

        FcmToken fcmToken;
        if (existingTokens.isEmpty()) {
            fcmToken = FcmToken.builder()
                    .token(testToken)
                    .userId(userDetails.getId())
                    .role(userDetails.getRole())
                    .deviceInfo("Test Device - Backend Testing")
                    .isActive(true)
                    .lastUsedAt(ZonedDateTime.now())
                    .build();
            fcmToken = fcmTokenRepository.save(fcmToken);
            log.info("Created test FCM token for user {}: {}", userDetails.getId(), testToken);
        } else {
            fcmToken = existingTokens.get(0);
            log.info("User {} already has FCM token: {}", userDetails.getId(), fcmToken.getToken());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userDetails.getId());
        response.put("role", userDetails.getRole().name());
        response.put("tokenId", fcmToken.getId());
        response.put("token", fcmToken.getToken());
        response.put("isActive", fcmToken.getIsActive());
        response.put("message", "Test FCM token registered. Note: This is a fake token, actual push won't be delivered to a device.");

        return ApiResponse.success("Test token registered", response);
    }

    /**
     * Complete test - registers a student token AND sends notification in one step.
     * This is the easiest way to test the full notification flow.
     */
    @PostMapping("/complete-test")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Complete notification test (register + send)",
            description = "Registers a test token for current user as STUDENT and immediately sends a Kuppi notification. " +
                    "This tests the complete flow in one API call."
    )
    public ApiResponse<Map<String, Object>> completeTest(
            @RequestParam(defaultValue = "Test Kuppi Session") String title,
            @RequestParam(defaultValue = "Computer Science") String subject,
            @RequestParam(defaultValue = "Test Host") String hostName,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.info("Running complete notification test for user {}", userDetails.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("firebaseEnabled", pushNotificationService.isEnabled());
        response.put("currentUser", Map.of(
                "id", userDetails.getId(),
                "role", userDetails.getRole().name()
        ));

        // Step 1: Register a test token for this user as STUDENT
        String testToken = "test-fcm-token-" + userDetails.getId() + "-" + System.currentTimeMillis();

        // Delete any existing tokens for this user first
        List<FcmToken> existingTokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userDetails.getId());
        existingTokens.forEach(t -> {
            t.setIsActive(false);
            fcmTokenRepository.save(t);
        });

        // Create new token with ROLE_STUDENT to receive notifications
        FcmToken fcmToken = FcmToken.builder()
                .token(testToken)
                .userId(userDetails.getId())
                .role(UserRole.ROLE_STUDENT)  // Force STUDENT role for testing
                .deviceInfo("Complete Test - " + LocalDateTime.now())
                .isActive(true)
                .lastUsedAt(ZonedDateTime.now())
                .build();
        fcmToken = fcmTokenRepository.save(fcmToken);

        response.put("registeredToken", Map.of(
                "tokenId", fcmToken.getId(),
                "role", "ROLE_STUDENT",
                "isActive", true
        ));

        // Step 2: Verify tokens exist
        List<FcmToken> studentTokens = fcmTokenRepository.findByRoleAndIsActiveTrue(UserRole.ROLE_STUDENT);
        response.put("totalStudentTokens", studentTokens.size());

        // Step 3: Send notification
        response.put("simulatedSession", Map.of(
                "title", title,
                "subject", subject,
                "hostName", hostName,
                "scheduledTime", LocalDateTime.now().plusDays(1)
        ));

        try {
            kuppiNotificationService.notifyNewKuppiSession(
                    999L,
                    title,
                    subject,
                    hostName,
                    LocalDateTime.now().plusDays(1)
            );

            response.put("notificationStatus", "SENT");
            response.put("message", "Test token registered as STUDENT and notification sent! " +
                    "Check logs for delivery status. Note: Test tokens will be rejected by Firebase (expected behavior).");

        } catch (Exception e) {
            response.put("notificationStatus", "ERROR");
            response.put("error", e.getMessage());
            log.error("Complete test failed", e);
        }

        return ApiResponse.success("Complete test executed", response);
    }

    /**
     * Simulate a Kuppi session creation notification.
     * This tests the notification flow without actually creating a session.
     */
    @PostMapping("/simulate-session-created")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Simulate Kuppi session created notification",
            description = "Send a test notification as if a new Kuppi session was created. Sends to all students."
    )
    public ApiResponse<Map<String, Object>> simulateSessionCreated(
            @RequestParam(defaultValue = "Test Kuppi Session") String title,
            @RequestParam(defaultValue = "Computer Science") String subject,
            @RequestParam(defaultValue = "Test Host") String hostName,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.info("Simulating Kuppi session creation notification by user {}", userDetails.getId());

        // Check how many students have tokens
        List<FcmToken> studentTokens = fcmTokenRepository.findByRoleAndIsActiveTrue(UserRole.ROLE_STUDENT);

        Map<String, Object> response = new HashMap<>();
        response.put("firebaseEnabled", pushNotificationService.isEnabled());
        response.put("totalStudentTokens", studentTokens.size());
        response.put("simulatedSession", Map.of(
                "title", title,
                "subject", subject,
                "hostName", hostName,
                "scheduledTime", LocalDateTime.now().plusDays(1)
        ));

        if (studentTokens.isEmpty()) {
            response.put("status", "NO_TOKENS");
            response.put("message", "No student FCM tokens found. Register test tokens first using /register-test-token");
            return ApiResponse.success("No tokens to send to", response);
        }

        // Send the notification
        try {
            kuppiNotificationService.notifyNewKuppiSession(
                    999L, // Fake session ID
                    title,
                    subject,
                    hostName,
                    LocalDateTime.now().plusDays(1)
            );

            response.put("status", "SENT");
            response.put("message", "Notification sent to " + studentTokens.size() + " student(s). Check logs for delivery status.");

            // Note about test tokens
            response.put("note", "If using test tokens, Firebase will report them as invalid. " +
                    "Real notifications require real FCM tokens from a browser/app.");

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            log.error("Failed to send test notification", e);
        }

        return ApiResponse.success("Simulation complete", response);
    }

    /**
     * Simulate a Kuppi session cancellation notification.
     */
    @PostMapping("/simulate-session-cancelled")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Simulate Kuppi session cancelled notification",
            description = "Send a test cancellation notification."
    )
    public ApiResponse<Map<String, Object>> simulateSessionCancelled(
            @RequestParam(defaultValue = "Test Kuppi Session") String title,
            @RequestParam(defaultValue = "Computer Science") String subject,
            @RequestParam(defaultValue = "Host unavailable") String reason,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.info("Simulating Kuppi session cancellation notification by user {}", userDetails.getId());

        List<FcmToken> studentTokens = fcmTokenRepository.findByRoleAndIsActiveTrue(UserRole.ROLE_STUDENT);

        Map<String, Object> response = new HashMap<>();
        response.put("firebaseEnabled", pushNotificationService.isEnabled());
        response.put("totalStudentTokens", studentTokens.size());

        if (studentTokens.isEmpty()) {
            response.put("status", "NO_TOKENS");
            response.put("message", "No student FCM tokens found.");
            return ApiResponse.success("No tokens to send to", response);
        }

        try {
            kuppiNotificationService.notifyKuppiSessionCancelled(999L, title, subject, reason);
            response.put("status", "SENT");
            response.put("message", "Cancellation notification sent");
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        }

        return ApiResponse.success("Simulation complete", response);
    }

    /**
     * Check notification system status and registered tokens.
     */
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get notification system status",
            description = "Check Firebase status and count of registered tokens."
    )
    public ApiResponse<Map<String, Object>> getStatus(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        Map<String, Object> status = new HashMap<>();

        // Firebase status
        status.put("firebaseEnabled", pushNotificationService.isEnabled());

        // Token counts by role
        List<FcmToken> allTokens = fcmTokenRepository.findByIsActiveTrue();
        long studentTokens = allTokens.stream().filter(t -> t.getRole() == UserRole.ROLE_STUDENT).count();
        long adminTokens = allTokens.stream().filter(t -> t.getRole() == UserRole.ROLE_ADMIN).count();

        status.put("totalActiveTokens", allTokens.size());
        status.put("studentTokens", studentTokens);
        status.put("adminTokens", adminTokens);

        // Current user's tokens
        List<FcmToken> myTokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userDetails.getId());
        status.put("myTokenCount", myTokens.size());
        status.put("myRole", userDetails.getRole().name());

        // Hints
        if (!pushNotificationService.isEnabled()) {
            status.put("hint", "Firebase is disabled. Check FIREBASE_ENABLED and FIREBASE_SERVICE_ACCOUNT_PATH.");
        } else if (studentTokens == 0) {
            status.put("hint", "No student tokens registered. Use /register-test-token to create one.");
        } else {
            status.put("hint", "System ready. Use /simulate-session-created to test notifications.");
        }

        return ApiResponse.success("Status retrieved", status);
    }

    /**
     * List all registered FCM tokens (for debugging).
     */
    @GetMapping("/tokens")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "List all FCM tokens",
            description = "List all registered FCM tokens for debugging."
    )
    public ApiResponse<List<Map<String, Object>>> listTokens() {
        List<FcmToken> tokens = fcmTokenRepository.findByIsActiveTrue();

        List<Map<String, Object>> tokenList = tokens.stream().map(t -> {
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("id", t.getId());
            tokenInfo.put("userId", t.getUserId());
            tokenInfo.put("role", t.getRole().name());
            tokenInfo.put("deviceInfo", t.getDeviceInfo());
            tokenInfo.put("isActive", t.getIsActive());
            tokenInfo.put("lastUsedAt", t.getLastUsedAt());
            tokenInfo.put("tokenPreview", t.getToken().substring(0, Math.min(20, t.getToken().length())) + "...");
            return tokenInfo;
        }).toList();

        return ApiResponse.success("Found " + tokens.size() + " tokens", tokenList);
    }

    /**
     * Clean up all test tokens.
     */
    @DeleteMapping("/tokens/cleanup")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Clean up test tokens",
            description = "Remove all test FCM tokens (tokens starting with 'test-fcm-token-')."
    )
    public ApiResponse<Map<String, Object>> cleanupTestTokens() {
        List<FcmToken> allTokens = fcmTokenRepository.findByIsActiveTrue();

        List<FcmToken> testTokens = allTokens.stream()
                .filter(t -> t.getToken().startsWith("test-fcm-token-"))
                .toList();

        testTokens.forEach(t -> {
            t.setIsActive(false);
            fcmTokenRepository.save(t);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("cleanedUp", testTokens.size());
        response.put("message", testTokens.size() + " test tokens deactivated");

        return ApiResponse.success("Cleanup complete", response);
    }
}
