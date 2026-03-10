package lk.iit.nextora.module.lostandfound.service;

/**
 * Service interface for sending notifications in the Lost & Found module.
 * Implementations can use email, push notifications, or in-app alerts.
 *
 * ✅ FIX: Added domain-specific methods — the original had only a generic notifyUser(String)
 *         which is too vague for a real notification system.
 */
public interface NotificationService {

    /**
     * Send a generic notification message to a user by their student ID.
     *
     * @param studentId  ID of the student to notify
     * @param message    Human-readable notification message
     */
    void notifyUser(Long studentId, String message);

    /**
     * Notify a student that a potential match was found for their lost item.
     * Called by ItemMatchingService when a high-confidence match is detected.
     *
     * @param studentId   ID of the student who reported the lost item
     * @param lostItemId  ID of their lost item
     * @param foundItemId ID of the found item that may match
     */
    void notifyMatchFound(Long studentId, Long lostItemId, Long foundItemId);

    /**
     * Notify a student that their claim has been approved by an admin.
     *
     * @param studentId  ID of the student whose claim was approved
     * @param claimId    ID of the approved claim
     */
    void notifyClaimApproved(Long studentId, Long claimId);

    /**
     * Notify a student that their claim was rejected with a reason.
     *
     * @param studentId  ID of the student whose claim was rejected
     * @param claimId    ID of the rejected claim
     * @param reason     Optional admin-provided reason for rejection
     */
    void notifyClaimRejected(Long studentId, Long claimId, String reason);
}