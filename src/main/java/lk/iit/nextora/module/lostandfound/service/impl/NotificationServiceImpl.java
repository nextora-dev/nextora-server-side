package lk.iit.nextora.module.lostandfound.service.impl;

// ── Service interface ───────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.service.NotificationService;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ── Spring ──────────────────────────────────────────────────────────────────
import org.springframework.stereotype.Service;

/**
 * Implementation of NotificationService.
 * Currently logs notifications via SLF4J.
 * In production, replace the log statements with actual delivery mechanisms:
 *   - Email via JavaMailSender
 *   - Push notifications via Firebase Cloud Messaging
 *   - In-app notifications via a Notification entity + WebSocket
 *
 * ✅ FIX: Was using System.out.println — NEVER use this in production code.
 *         Replaced with @Slf4j structured logging, matching the Kuppi pattern.
 * ✅ FIX: Added @RequiredArgsConstructor — ready for injecting email/push services later.
 * ✅ FIX: Implemented all the domain-specific methods from the new NotificationService interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyUser(Long studentId, String message) {
        // ✅ FIX: replaced System.out.println with structured log — includes student ID
        //         for traceability, and uses SLF4J's {} placeholder (not string concat)
        log.info("NOTIFICATION → student {}: {}", studentId, message);

        // TODO: Replace with real delivery:
        // emailService.send(studentId, "Lost & Found Notification", message);
        // pushService.send(studentId, message);
    }

    @Override
    public void notifyMatchFound(Long studentId, Long lostItemId, Long foundItemId) {
        // ✅ FIX: was completely missing — matching service now calls this
        String message = String.format(
                "A potential match was found for your lost item #%d (found item #%d). Please check your matches.",
                lostItemId, foundItemId);
        log.info("MATCH NOTIFICATION → student {}: lostItem {} matched foundItem {}",
                studentId, lostItemId, foundItemId);

        // TODO: notifyUser(studentId, message);
    }

    @Override
    public void notifyClaimApproved(Long studentId, Long claimId) {
        // ✅ FIX: was completely missing — ClaimServiceImpl.approveClaim() now calls this
        log.info("CLAIM APPROVED NOTIFICATION → student {}: claim {} approved", studentId, claimId);

        // TODO: notifyUser(studentId, "Your claim #" + claimId + " has been approved. You can collect your item.");
    }

    @Override
    public void notifyClaimRejected(Long studentId, Long claimId, String reason) {
        // ✅ FIX: was completely missing — ClaimServiceImpl.rejectClaim() now calls this
        log.info("CLAIM REJECTED NOTIFICATION → student {}: claim {} rejected. Reason: {}",
                studentId, claimId, reason);

        // TODO: notifyUser(studentId, "Your claim #" + claimId + " was rejected. Reason: " + reason);
    }
}