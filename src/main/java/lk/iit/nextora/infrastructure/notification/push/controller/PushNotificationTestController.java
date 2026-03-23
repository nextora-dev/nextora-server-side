package lk.iit.nextora.infrastructure.notification.push.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller for Push Notifications.
 *
 * This controller is ONLY available in development environment (dev profile).
 * It provides easy testing endpoints that don't require complex payloads.
 *
 * ⚠️ DO NOT ENABLE IN PRODUCTION
 */
@RestController
@RequestMapping("/api/v1/push/test")
@RequiredArgsConstructor
@Slf4j
@Profile("dev") // Only available in development
@Tag(name = "Push Notifications - Testing", description = "Development-only endpoints for testing push notifications")
@SecurityRequirement(name = "bearerAuth")
public class PushNotificationTestController {

    private final PushNotificationService pushNotificationService;

    /**
     * Send a test notification to yourself (the logged-in user).
     * This is the easiest way to test if push notifications are working.
     */
    @PostMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Send test notification to yourself",
            description = "Send a test push notification to your own registered devices. " +
                    "Make sure you have registered an FCM token first."
    )
    public ApiResponse<NotificationResponse> sendTestToSelf(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.info("Sending test notification to self: userId={}", userDetails.getId());

        Map<String, String> data = new HashMap<>();
        data.put("type", "test");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        NotificationResponse response = pushNotificationService.sendToUser(
                userDetails.getId(),
                "🧪 Test Notification",
                "This is a test notification from Nextora! If you see this, push notifications are working correctly.",
                data
        );

        return ApiResponse.success(response.getMessage(), response);
    }

    /**
     * Send a test notification with custom message to yourself.
     */
    @PostMapping("/self/custom")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Send custom test notification to yourself",
            description = "Send a custom push notification to your own registered devices."
    )
    public ApiResponse<NotificationResponse> sendCustomTestToSelf(
            @RequestParam(defaultValue = "Test Title") String title,
            @RequestParam(defaultValue = "Test Body") String body,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.info("Sending custom test notification to self: userId={}, title={}", userDetails.getId(), title);

        Map<String, String> data = new HashMap<>();
        data.put("type", "custom_test");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        NotificationResponse response = pushNotificationService.sendToUser(
                userDetails.getId(),
                title,
                body,
                data
        );

        return ApiResponse.success(response.getMessage(), response);
    }

    /**
     * Get diagnostic information about push notification status.
     */
    @GetMapping("/diagnostics")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get push notification diagnostics",
            description = "Get detailed diagnostic information about the push notification system status."
    )
    public ApiResponse<Map<String, Object>> getDiagnostics(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        Map<String, Object> diagnostics = new HashMap<>();

        diagnostics.put("firebaseEnabled", pushNotificationService.isEnabled());
        diagnostics.put("currentUserId", userDetails.getId());
        diagnostics.put("currentUserRole", userDetails.getRole().name());
        diagnostics.put("timestamp", System.currentTimeMillis());

        // Add hints for troubleshooting
        if (!pushNotificationService.isEnabled()) {
            diagnostics.put("hint", "Firebase is not enabled. Check FIREBASE_ENABLED and FIREBASE_SERVICE_ACCOUNT_PATH environment variables.");
        } else {
            diagnostics.put("hint", "Firebase is enabled. Make sure you have registered an FCM token from the frontend.");
        }

        return ApiResponse.success("Diagnostics retrieved", diagnostics);
    }

    /**
     * Simple ping to verify the endpoint is working.
     */
    @GetMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Ping test endpoint",
            description = "Simple ping to verify the test endpoints are accessible."
    )
    public ApiResponse<String> ping() {
        return ApiResponse.success("pong", "Push notification test endpoints are active (dev mode)");
    }
}
