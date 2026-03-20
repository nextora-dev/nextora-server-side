package lk.iit.nextora.infrastructure.notification.push.service.impl;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.request.FcmTokenRequest;
import lk.iit.nextora.infrastructure.notification.push.entity.FcmToken;
import lk.iit.nextora.infrastructure.notification.push.repository.FcmTokenRepository;
import lk.iit.nextora.infrastructure.notification.push.service.FcmTokenService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Implementation of FCM Token Management Service.
 *
 * Handles all FCM token lifecycle operations including:
 * - Token registration and updates
 * - Token deactivation (soft delete)
 * - Token cleanup for maintenance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenServiceImpl implements FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Override
    @Transactional
    public FcmToken registerToken(FcmTokenRequest request, BaseUser user) {
        String token = request.getToken();
        Long userId = user.getId();
        UserRole role = user.getRole();

        Optional<FcmToken> existingToken = fcmTokenRepository.findByToken(token);

        if (existingToken.isPresent()) {
            FcmToken fcmToken = existingToken.get();

            if (fcmToken.getUserId().equals(userId)) {
                log.debug("Reactivating existing token for user: {}", userId);
                fcmToken.setIsActive(true);
                fcmToken.setRole(role);
                fcmToken.setDeviceType(request.getDeviceType());
                return fcmTokenRepository.save(fcmToken);
            } else {
                // Token exists for different user - transfer ownership
                // This can happen if user logs out and another logs in on same device
                log.debug("Transferring token ownership from user {} to user {}",
                        fcmToken.getUserId(), userId);
                fcmToken.setUserId(userId);
                fcmToken.setRole(role);
                fcmToken.setIsActive(true);
                fcmToken.setDeviceType(request.getDeviceType());
                return fcmTokenRepository.save(fcmToken);
            }
        }

        log.debug("Registering new FCM token for user: {}", userId);
        FcmToken newToken = FcmToken.builder()
                .token(token)
                .userId(userId)
                .role(role)
                .deviceType(request.getDeviceType())
                .isActive(true)
                .build();

        return fcmTokenRepository.save(newToken);
    }

    @Override
    @Transactional
    public void deactivateToken(String token) {
        log.debug("Deactivating FCM token");
        fcmTokenRepository.deactivateToken(token, ZonedDateTime.now());
    }

    @Override
    @Transactional
    public void deactivateAllTokensForUser(Long userId) {
        log.debug("Deactivating all FCM tokens for user: {}", userId);
        int count = fcmTokenRepository.deactivateAllTokensForUser(userId, ZonedDateTime.now());
        log.info("Deactivated {} tokens for user: {}", count, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveTokensByRole(UserRole role) {
        return fcmTokenRepository.countActiveTokensByRole(role);
    }

    @Override
    @Transactional
    public int cleanupInactiveTokens(int daysOld) {
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(daysOld);
        int deleted = fcmTokenRepository.deleteStaleTokens(cutoffDate);
        log.info("Cleaned up {} inactive tokens older than {} days", deleted, daysOld);
        return deleted;
    }
}
