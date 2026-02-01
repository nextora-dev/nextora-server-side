package lk.iit.nextora.infrastructure.notification.push.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.infrastructure.notification.push.dto.request.RegisterTokenRequest;
import lk.iit.nextora.infrastructure.notification.push.dto.request.SendNotificationRequest;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.dto.response.TokenRegistrationResponse;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Push Notification operations.
 *
 * Endpoints:
 * - POST /api/v1/push/token   : Register/update FCM token (authenticated users)
 * - DELETE /api/v1/push/token : Remove FCM token on logout (authenticated users)
 * - POST /api/v1/push/send    : Send notifications (admin only)
 * - GET /api/v1/push/status   : Check if push notifications are enabled
 *
 * Security:
 * - All endpoints require JWT authentication
 * - Send notification requires PUSH_SEND permission (admin roles only)
 * - Token management is available to all authenticated users
 */
@RestController
@RequestMapping(ApiConstants.PUSH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Push Notifications", description = "FCM Push Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    /**
     * Register or update an FCM token for the current user.
     * Called by the frontend after obtaining a token from Firebase.
     *
     * Handles:
     * - New token registration
     * - Token refresh (same device, new token)
     * - Device switching (token reassignment to different user)
     */
    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Register FCM token",
            description = "Register or update an FCM push notification token for the authenticated user. " +
                    "Call this after obtaining a token from Firebase on the frontend. " +
                    "Tokens are automatically managed for multi-device support."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ApiResponse<TokenRegistrationResponse> registerToken(
            @Valid @RequestBody RegisterTokenRequest request,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.debug("Registering FCM token for user: {}", userDetails.getUsername());

        TokenRegistrationResponse response = pushNotificationService.registerToken(
                userDetails.getId(),
                userDetails.getRole(),
                request
        );

        return ApiResponse.success(response.getMessage(), response);
    }

    /**
     * Remove/deactivate an FCM token.
     * Should be called on user logout to prevent notifications to logged-out devices.
     */
    @DeleteMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Remove FCM token",
            description = "Remove an FCM token on logout. This prevents notifications from being " +
                    "sent to the device after the user has logged out."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ApiResponse<Void> removeToken(
            @RequestParam String token,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.debug("Removing FCM token for user: {}", userDetails.getUsername());

        pushNotificationService.removeToken(token);

        return ApiResponse.success("FCM token removed successfully", null);
    }

    /**
     * Remove all FCM tokens for the current user.
     * Useful for "logout from all devices" functionality.
     */
    @DeleteMapping("/token/all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Remove all FCM tokens",
            description = "Remove all FCM tokens for the current user. Use this for 'logout from all devices' functionality."
    )
    public ApiResponse<Void> removeAllTokens(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.debug("Removing all FCM tokens for user: {}", userDetails.getUsername());

        pushNotificationService.removeAllTokensForUser(userDetails.getId());

        return ApiResponse.success("All FCM tokens removed successfully", null);
    }

    /**
     * Send push notification to specified targets.
     * Admin-only endpoint for sending announcements, alerts, etc.
     *
     * Targeting options (in priority order):
     * 1. userIds - Send to specific users
     * 2. targetRole - Send to all users with a role
     * 3. topic - Send to a topic (future feature)
     */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PUSH:SEND')")
    @Operation(
            summary = "Send push notification (Admin)",
            description = "Send push notifications to specified targets. Requires PUSH_SEND permission. " +
                    "Target by userIds, role, or topic. Priority: userIds > targetRole > topic."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications sent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized - requires PUSH_SEND permission")
    })
    public ApiResponse<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        log.info("Admin {} sending push notification: title={}",
                userDetails.getUsername(), request.getTitle());

        NotificationResponse response = pushNotificationService.send(request);

        return ApiResponse.success(response.getMessage(), response);
    }

    /**
     * Check if push notifications are enabled and configured.
     * Useful for frontend to know whether to request notification permissions.
     */
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Check push notification status",
            description = "Check if push notifications are enabled and Firebase is configured. " +
                    "Frontend can use this to decide whether to request notification permissions."
    )
    public ApiResponse<PushStatusResponse> getStatus() {
        boolean enabled = pushNotificationService.isEnabled();
        return ApiResponse.success(new PushStatusResponse(enabled));
    }

    /**
     * Simple response for push status endpoint.
     */
    public record PushStatusResponse(boolean enabled) {}
}
