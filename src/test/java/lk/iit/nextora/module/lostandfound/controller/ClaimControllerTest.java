package lk.iit.nextora.module.lostandfound.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import lk.iit.nextora.module.lostandfound.service.ClaimService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimController Unit Tests")
class ClaimControllerTest {

    @Mock private ClaimService claimService;
    @InjectMocks private ClaimController controller;

    @Nested
    @DisplayName("createClaim")
    class CreateClaimTests {

        @Test
        @DisplayName("Should submit claim successfully")
        void createClaim_success() {
            CreateClaimRequest request = CreateClaimRequest.builder()
                    .lostItemId(1L)
                    .foundItemId(2L)
                    .proofDescription("Same phone with unique scratches")
                    .build();

            ClaimResponse response = ClaimResponse.builder()
                    .id(1L)
                    .status("PENDING")
                    .build();

            when(claimService.createClaim(any(CreateClaimRequest.class)))
                    .thenReturn(response);

            ApiResponse<ClaimResponse> result = controller.createClaim(request);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getId()).isEqualTo(1L);
            assertThat(result.getData().getStatus()).isEqualTo("PENDING");
            verify(claimService, times(1)).createClaim(any(CreateClaimRequest.class));
        }
    }

    @Nested
    @DisplayName("getClaimById")
    class GetClaimByIdTests {

        @Test
        @DisplayName("Should return claim by ID")
        void getClaimById_success() {
            Long claimId = 1L;
            ClaimResponse response = ClaimResponse.builder()
                    .id(claimId)
                    .status("PENDING")
                    .build();

            when(claimService.getClaimById(claimId)).thenReturn(response);

            ApiResponse<ClaimResponse> result = controller.getClaim(claimId);

            assertThat(result).isNotNull();
            assertThat(result.getData().getId()).isEqualTo(claimId);
            verify(claimService, times(1)).getClaimById(claimId);
        }
    }

    @Nested
    @DisplayName("getMyClaims")
    class GetMyClaimsTests {

        @Test
        @DisplayName("Should return paginated claims for current user")
        void getMyClaims_success() {
            Pageable pageable = PageRequest.of(0, 10);
            ClaimResponse claim = ClaimResponse.builder()
                    .id(1L)
                    .status("PENDING")
                    .build();

            PagedResponse<ClaimResponse> response = PagedResponse.of(new org.springframework.data.domain.PageImpl<>(List.of(claim), pageable, 1));

            when(claimService.getMyClaims(any(Pageable.class)))
                    .thenReturn(response);

            ApiResponse<PagedResponse<ClaimResponse>> result = controller.getMyClaims(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            verify(claimService, times(1)).getMyClaims(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getClaimsByStatus")
    class GetClaimsByStatusTests {

        @Test
        @DisplayName("Should return claims filtered by status")
        void getClaimsByStatus_success() {
            Pageable pageable = PageRequest.of(0, 10);
            ClaimResponse claim = ClaimResponse.builder()
                    .id(1L)
                    .status("PENDING")
                    .build();

            PagedResponse<ClaimResponse> response = PagedResponse.of(new org.springframework.data.domain.PageImpl<>(List.of(claim), pageable, 1));

            when(claimService.getClaimsByStatus("PENDING", pageable))
                    .thenReturn(response);

            ApiResponse<PagedResponse<ClaimResponse>> result = controller.getClaimsByStatus("PENDING", 0, 10);

            assertThat(result).isNotNull();
            verify(claimService, times(1)).getClaimsByStatus("PENDING", pageable);
        }
    }

    @Nested
    @DisplayName("approveClaim")
    class ApproveClaimTests {

        @Test
        @DisplayName("Should approve claim successfully")
        void approveClaim_success() {
            Long claimId = 1L;
            ClaimResponse response = ClaimResponse.builder()
                    .id(claimId)
                    .status("APPROVED")
                    .build();

            when(claimService.approveClaim(claimId)).thenReturn(response);

            ApiResponse<ClaimResponse> result = controller.approveClaim(claimId);

            assertThat(result).isNotNull();
            assertThat(result.getData().getStatus()).isEqualTo("APPROVED");
            verify(claimService, times(1)).approveClaim(claimId);
        }
    }

    @Nested
    @DisplayName("rejectClaim")
    class RejectClaimTests {

        @Test
        @DisplayName("Should reject claim successfully")
        void rejectClaim_success() {
            Long claimId = 1L;
            String rejectionReason = "Insufficient proof provided";
            ClaimResponse response = ClaimResponse.builder()
                    .id(claimId)
                    .status("REJECTED")
                    .build();

            when(claimService.rejectClaim(eq(claimId), anyString()))
                    .thenReturn(response);

            ApiResponse<ClaimResponse> result = controller.rejectClaim(claimId, rejectionReason);

            assertThat(result).isNotNull();
            assertThat(result.getData().getStatus()).isEqualTo("REJECTED");
            verify(claimService, times(1)).rejectClaim(eq(claimId), anyString());
        }
    }
}

