package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import lk.iit.nextora.module.lostandfound.entity.Claim;
import lk.iit.nextora.module.lostandfound.mapper.ClaimMapper;
import lk.iit.nextora.module.lostandfound.repository.ClaimRepository;
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;
import lk.iit.nextora.module.lostandfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;

    @Override
    public ClaimResponse createClaim(CreateClaimRequest request) {
        Claim claim = new Claim();
        claim.setLostItem(lostItemRepository.findById(request.getLostItemId()).orElseThrow());
        claim.setFoundItem(foundItemRepository.findById(request.getFoundItemId()).orElseThrow());
        claim.setStatus("PENDING");

        return ClaimMapper.toResponse(
                claimRepository.save(claim)
        );
    }

    @Override
    public ClaimResponse getClaimById(Long id) {
        return ClaimMapper.toResponse(
                claimRepository.findById(id).orElseThrow()
        );
    }
}
