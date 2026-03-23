package lk.iit.nextora.infrastructure.notification.push.service.impl;

import jakarta.persistence.EntityManager;
import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.infrastructure.notification.push.entity.Notification;
import lk.iit.nextora.infrastructure.notification.push.repository.NotificationRepository;
import lk.iit.nextora.infrastructure.notification.push.service.NotificationHistoryService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Notification History Service.
 *
 * Handles storing and retrieving notification history for users,
 * enabling users to view past notifications even if they missed them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private final NotificationRepository notificationRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Notification storeNotification(Long userId, String title, String body, NotificationType type,
                                          String clickAction, String imageUrl, Map<String, String> data) {
        BaseUser user = entityManager.find(BaseUser.class, userId);
        if (user == null) {
            log.warn("Cannot store notification: User not found with id {}", userId);
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(type)
                .clickAction(clickAction)
                .imageUrl(imageUrl)
                .data(data)
                .sentAt(ZonedDateTime.now())
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.debug("Stored notification {} for user {}", saved.getId(), userId);
        return saved;
    }

    @Override
    @Transactional
    public void storeNotificationForUsers(List<Long> userIds, String title, String body, NotificationType type,
                                          String clickAction, String imageUrl, Map<String, String> data) {
        ZonedDateTime now = ZonedDateTime.now();
        int storedCount = 0;

        for (Long userId : userIds) {
            BaseUser user = entityManager.find(BaseUser.class, userId);
            if (user != null) {
                Notification notification = Notification.builder()
                        .user(user)
                        .title(title)
                        .body(body)
                        .type(type)
                        .clickAction(clickAction)
                        .imageUrl(imageUrl)
                        .data(data)
                        .sentAt(now)
                        .read(false)
                        .build();
                notificationRepository.save(notification);
                storedCount++;
            }
        }

        log.info("Stored notification for {} of {} users", storedCount, userIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderBySentAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
            log.debug("Marked notification {} as read", notificationId);
        });
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsReadByUserId(userId, ZonedDateTime.now());
        log.debug("Marked {} notifications as read for user {}", count, userId);
        return count;
    }

    @Override
    @Transactional
    public int deleteOldNotifications(int daysOld) {
        ZonedDateTime cutoff = ZonedDateTime.now().minusDays(daysOld);
        int deleted = notificationRepository.deleteOlderThan(cutoff);
        log.info("Deleted {} notifications older than {} days", deleted, daysOld);
        return deleted;
    }
}
