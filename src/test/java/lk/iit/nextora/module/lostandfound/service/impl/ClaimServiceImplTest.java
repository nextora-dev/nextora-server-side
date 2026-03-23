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
import lk.iit.nextora.module.lostandfound.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimServiceImpl Unit Tests")
class ClaimServiceImplTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private LostItemRepository lostItemRepository;
    @Mock private FoundItemRepository foundItemRepository;
    @Mock private ClaimMapper claimMapper;
    @Mock private SecurityService securityService;
    @Mock private NotificationService notificationService;

    @InjectMocks private ClaimServiceImpl claimService;

    @Nested
    @DisplayName("createClaim")
    class CreateClaimTests {

        @Test
        @DisplayName("Should create claim successfully")
        void createClaim_success() {
            Long claimantId = 100L;
            CreateClaimRequest request = CreateClaimRequest.builder()
                    .lostItemId(1L)
                    .foundItemId(2L)
                    .proofDescription("Same phone with scratches")
                    .build();

            LostItem lostItem = LostItem.builder().id(1L).title("Lost iPhone").build();
            FoundItem foundItem = FoundItem.builder().id(2L).title("Found iPhone").build();
            Claim claim = Claim.builder().id(1L).claimantId(claimantId).status("PENDING").build();
            ClaimResponse response = ClaimResponse.builder().id(1L).status("PENDING").build();

            when(securityService.getCurrentUserId()).thenReturn(claimantId);
            when(lostItemRepository.findById(1L)).thenReturn(Optional.of(lostItem));
            when(foundItemRepository.findById(2L)).thenReturn(Optional.of(foundItem));
            when(claimRepository.findByLostItemId(1L)).thenReturn(List.of());
            when(claimRepository.save(any(Claim.class))).thenReturn(claim);
            when(claimMapper.toResponse(claim)).thenReturn(response);

            ClaimResponse result = claimService.createClaim(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(claimRepository, times(1)).save(any(Claim.class));
        }

        @Test
        @DisplayName("Should throw exception when lost item not found")
        void createClaim_lostItemNotFound() {
            CreateClaimRequest request = CreateClaimRequest.builder()
                    .lostItemId(999L)
                    .foundItemId(2L)
                    .proofDescription("Test")
                    .build();

            when(securityService.getCurrentUserId()).thenReturn(100L);
            when(lostItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> claimService.createClaim(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when duplicate claim exists")
        void createClaim_duplicateClaim() {
            Long claimantId = 100L;
            CreateClaimRequest request = CreateClaimRequest.builder()
                    .lostItemId(1L)
                    .foundItemId(2L)
                    .proofDescription("Test")
                    .build();

            LostItem lostItem = LostItem.builder().id(1L).title("Lost Item").build();
            FoundItem foundItem = FoundItem.builder().id(2L).title("Found Item").build();
            Claim existing = Claim.builder().id(1L).lostItem(lostItem).foundItem(foundItem).claimantId(claimantId).build();

            when(securityService.getCurrentUserId()).thenReturn(claimantId);
            when(lostItemRepository.findById(1L)).thenReturn(Optional.of(lostItem));
            when(foundItemRepository.findById(2L)).thenReturn(Optional.of(foundItem));
            when(claimRepository.findByLostItemId(1L)).thenReturn(List.of(existing));

            assertThatThrownBy(() -> claimService.createClaim(request))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("getClaimById")
    class GetClaimByIdTests {

        @Test
        @DisplayName("Should return claim by ID")
        void getClaimById_success() {
            Claim claim = Claim.builder().id(1L).status("PENDING").build();
            ClaimResponse response = ClaimResponse.builder().id(1L).status("PENDING").build();

            when(claimRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(claim));
            when(claimMapper.toResponse(claim)).thenReturn(response);

            ClaimResponse result = claimService.getClaimById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when claim not found")
        void getClaimById_notFound() {
            when(claimRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> claimService.getClaimById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getMyClaims")
    class GetMyClaimsTests {

        @Test
        @DisplayName("Should return paginated claims for current user")
        void getMyClaims_success() {
            Long claimantId = 100L;
            Pageable pageable = PageRequest.of(0, 10);
            Claim claim = Claim.builder().id(1L).claimantId(claimantId).status("PENDING").build();
            Page<Claim> page = new PageImpl<>(List.of(claim), pageable, 1);
            ClaimResponse response = ClaimResponse.builder().id(1L).status("PENDING").build();

            when(securityService.getCurrentUserId()).thenReturn(claimantId);
            when(claimRepository.findByClaimantId(claimantId, pageable)).thenReturn(page);
            when(claimMapper.toResponse(claim)).thenReturn(response);

            PagedResponse<ClaimResponse> result = claimService.getMyClaims(pageable);

            assertThat(result).isNotNull();
            verify(claimRepository).findByClaimantId(claimantId, pageable);
        }
    }

    @Nested
    @DisplayName("getClaimsByStatus")
    class GetClaimsByStatusTests {

        @Test
        @DisplayName("Should return claims filtered by status")
        void getClaimsByStatus_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Claim claim = Claim.builder().id(1L).status("PENDING").build();
            Page<Claim> page = new PageImpl<>(List.of(claim), pageable, 1);
            ClaimResponse response = ClaimResponse.builder().id(1L).status("PENDING").build();

            when(claimRepository.findByStatus("PENDING", pageable)).thenReturn(page);
            when(claimMapper.toResponse(claim)).thenReturn(response);

            PagedResponse<ClaimResponse> result = claimService.getClaimsByStatus("PENDING", pageable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("approveClaim")
    class ApproveClaimTests {

        @Test
        @DisplayName("Should approve pending claim")
        void approveClaim_success() {
            Claim claim = Claim.builder().id(1L).status("PENDING").build();
            ClaimResponse response = ClaimResponse.builder().id(1L).status("APPROVED").build();

            when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
            when(claimRepository.save(any(Claim.class))).thenReturn(claim);
            when(claimMapper.toResponse(claim)).thenReturn(response);

            ClaimResponse result = claimService.approveClaim(1L);

            assertThat(result).isNotNull();
            verify(claimRepository).save(any(Claim.class));
        }

        @Test
        @DisplayName("Should throw when claim not found")
        void approveClaim_notFound() {
            when(claimRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> claimService.approveClaim(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("rejectClaim")
    class RejectClaimTests {

        @Test
        @DisplayName("Should reject pending claim")
        void rejectClaim_success() {
            Claim claim = Claim.builder().id(1L).status("PENDING").build();
            ClaimResponse response = ClaimResponse.builder().id(1L).status("REJECTED").build();

            when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
            when(claimRepository.save(any(Claim.class))).thenReturn(claim);
            when(claimMapper.toResponse(claim)).thenReturn(response);

            ClaimResponse result = claimService.rejectClaim(1L, "Invalid proof");

            assertThat(result).isNotNull();
            verify(claimRepository).save(any(Claim.class));
        }
    }
}


