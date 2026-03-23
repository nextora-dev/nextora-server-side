package lk.iit.nextora.module.lostandfound.service;

public interface NotificationService {

    void notifyUser(Long studentId, String message);

    void notifyMatchFound(Long studentId, Long lostItemId, Long foundItemId);

    void notifyClaimApproved(Long studentId, Long claimId);

    void notifyClaimRejected(Long studentId, Long claimId, String reason);
}
