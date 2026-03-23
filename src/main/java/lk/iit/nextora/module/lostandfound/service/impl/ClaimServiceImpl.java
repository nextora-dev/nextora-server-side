package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import lk.iit.nextora.module.lostandfound.entity.Claim;
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import lk.iit.nextora.module.lostandfound.entity.LostItem;
import lk.iit.nextora.module.lostandfound.mapper.ClaimMapper;
import lk.iit.nextora.module.lostandfound.repository.ClaimRepository;
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;
import lk.iit.nextora.module.lostandfound.service.ClaimService;
import lk.iit.nextora.module.lostandfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimMapper claimMapper;
    private final SecurityService securityService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest request) {
        Long currentStudentId = securityService.getCurrentUserId();

        LostItem lostItem = lostItemRepository.findById(request.getLostItemId())
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", request.getLostItemId()));

        FoundItem foundItem = foundItemRepository.findById(request.getFoundItemId())
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", request.getFoundItemId()));

        boolean alreadyClaimed = claimRepository
                .findByLostItemId(lostItem.getId())
                .stream()
                .anyMatch(c -> c.getFoundItem() != null
                        && c.getFoundItem().getId().equals(foundItem.getId())
                        && c.getClaimantId().equals(currentStudentId));

        if (alreadyClaimed) {
            throw new BadRequestException("You have already submitted a claim for this item pair");
        }

        Claim claim = Claim.builder()
                .lostItem(lostItem)
                .foundItem(foundItem)
                .claimantId(currentStudentId)
                .proofDescription(request.getProofDescription())
                .status("PENDING")
                .build();

        Claim saved = claimRepository.save(claim);
        log.info("Claim {} created by student {} for lostItem {} / foundItem {}",
                saved.getId(), currentStudentId, lostItem.getId(), foundItem.getId());

        return claimMapper.toResponse(saved);
    }

    @Override
    public ClaimResponse getClaimById(Long id) {
        Claim claim = claimRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", id));
        return claimMapper.toResponse(claim);
    }

    @Override
    public PagedResponse<ClaimResponse> getMyClaims(Pageable pageable) {
        Long currentStudentId = securityService.getCurrentUserId();
        Page<Claim> page = claimRepository.findByClaimantId(currentStudentId, pageable);
        return PagedResponse.of(page.map(claimMapper::toResponse));
    }

    @Override
    public PagedResponse<ClaimResponse> getClaimsByStatus(String status, Pageable pageable) {
        Page<Claim> page = claimRepository.findByStatus(status, pageable);
        return PagedResponse.of(page.map(claimMapper::toResponse));
    }

    @Override
    @Transactional
    public ClaimResponse approveClaim(Long id) {
        Claim claim = findClaimById(id);

        if (!"PENDING".equals(claim.getStatus())) {
            throw new BadRequestException("Only PENDING claims can be approved");
        }

        claim.setStatus("APPROVED");
        Claim saved = claimRepository.save(claim);

        LostItem lostItem = claim.getLostItem();
        lostItem.setActive(false);
        lostItemRepository.save(lostItem);

        FoundItem foundItem = claim.getFoundItem();
        foundItem.setActive(false);
        foundItemRepository.save(foundItem);

        notificationService.notifyClaimApproved(claim.getClaimantId(), saved.getId());

        log.info("Claim {} approved — lostItem {} and foundItem {} marked inactive",
                id, lostItem.getId(), foundItem.getId());

        return claimMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ClaimResponse rejectClaim(Long id, String reason) {
        Claim claim = findClaimById(id);

        if (!"PENDING".equals(claim.getStatus())) {
            throw new BadRequestException("Only PENDING claims can be rejected");
        }

        claim.setStatus("REJECTED");
        claim.setRejectionReason(reason);
        Claim saved = claimRepository.save(claim);

        notificationService.notifyClaimRejected(claim.getClaimantId(), saved.getId(), reason);

        log.info("Claim {} rejected. Reason: {}", id, reason);

        return claimMapper.toResponse(saved);
    }

    private Claim findClaimById(Long id) {
        return claimRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", id));
    }
}