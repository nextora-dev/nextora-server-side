package lk.iit.nextora.infrastructure.notification.push.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationHistoryResponse;
import lk.iit.nextora.infrastructure.notification.push.entity.Notification;
import lk.iit.nextora.infrastructure.notification.push.service.NotificationHistoryService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Student Notification History.
 *
 * Provides endpoints for students to:
 * - View all their notifications (with pagination)
 * - View unread notifications
 * - Get unread notification count (badge count)
 * - Mark notifications as read
 * - Mark all notifications as read
 *
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping(ApiConstants.NOTIFICATIONS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Student notification history endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationHistoryController {

    private final NotificationHistoryService notificationHistoryService;

    /**
     * Get all notifications for the current user with pagination.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get my notifications",
            description = "Get all notifications for the current user, ordered by most recent. Supports pagination."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ApiResponse<Page<NotificationHistoryResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        Page<Notification> notifications = notificationHistoryService.getNotificationsForUser(
                userDetails.getId(), page, size
        );

        Page<NotificationHistoryResponse> response = notifications.map(NotificationHistoryResponse::from);
        return ApiResponse.success("Notifications retrieved", response);
    }

    /**
     * Get unread notifications for the current user.
     */
    @GetMapping("/unread")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get unread notifications",
            description = "Get all unread notifications for the current user."
    )
    public ApiResponse<List<NotificationHistoryResponse>> getUnreadNotifications(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        List<Notification> unread = notificationHistoryService.getUnreadNotificationsForUser(userDetails.getId());
        List<NotificationHistoryResponse> response = unread.stream()
                .map(NotificationHistoryResponse::from)
                .toList();
        return ApiResponse.success("Unread notifications retrieved", response);
    }

    /**
     * Get unread notification count (for badge display).
     */
    @GetMapping("/unread/count")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get unread count",
            description = "Get the count of unread notifications. Use this for badge/counter display."
    )
    public ApiResponse<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        long count = notificationHistoryService.getUnreadCount(userDetails.getId());
        return ApiResponse.success("Unread count retrieved", Map.of("unreadCount", count));
    }

    /**
     * Mark a specific notification as read.
     */
    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read."
    )
    public ApiResponse<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        notificationHistoryService.markAsRead(notificationId);
        return ApiResponse.success("Notification marked as read");
    }

    /**
     * Mark all notifications as read for the current user.
     */
    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all notifications as read for the current user."
    )
    public ApiResponse<Map<String, Integer>> markAllAsRead(
            @AuthenticationPrincipal BaseUser userDetails
    ) {
        int count = notificationHistoryService.markAllAsRead(userDetails.getId());
        return ApiResponse.success("All notifications marked as read", Map.of("markedAsRead", count));
    }
}

