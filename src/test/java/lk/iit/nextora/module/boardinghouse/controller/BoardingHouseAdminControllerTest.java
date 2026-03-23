package lk.iit.nextora.module.boardinghouse.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseStatsResponse;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardingHouseAdminController Unit Tests")
class BoardingHouseAdminControllerTest {

    @Mock
    private BoardingHouseService boardingHouseService;

    @InjectMocks
    private BoardingHouseAdminController controller;

    // ============================================================
    // ADMIN UPDATE TESTS
    // ============================================================

    @Nested
    @DisplayName("PUT /api/v1/admin/boarding-houses/{id} (admin update)")
    class AdminUpdateTests {

        @Test
        @DisplayName("Should update boarding house successfully with all fields")
        void adminUpdate_allFields_success() {
            // Given
            Long houseId = 1L;
            UpdateBoardingHouseRequest request = UpdateBoardingHouseRequest.builder()
                    .title("Updated Premium House")
                    .description("Updated description")
                    .availableRooms(5)
                    .build();

            BoardingHouseResponse expectedResponse = BoardingHouseResponse.builder()
                    .id(houseId)
                    .title("Updated Premium House")
                    .description("Updated description")
                    .availableRooms(5)
                    .build();

            when(boardingHouseService.adminUpdate(eq(houseId), any(UpdateBoardingHouseRequest.class)))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<BoardingHouseResponse> response = controller.adminUpdate(houseId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getId()).isEqualTo(houseId);
            assertThat(response.getData().getTitle()).isEqualTo("Updated Premium House");
            assertThat(response.getData().getAvailableRooms()).isEqualTo(5);
            verify(boardingHouseService, times(1)).adminUpdate(eq(houseId), any(UpdateBoardingHouseRequest.class));
        }

        @Test
        @DisplayName("Should update only specific fields (partial update)")
        void adminUpdate_partialFields_success() {
            // Given
            Long houseId = 2L;
            UpdateBoardingHouseRequest request = UpdateBoardingHouseRequest.builder()
                    .availableRooms(3)
                    .build();

            BoardingHouseResponse expectedResponse = BoardingHouseResponse.builder()
                    .id(houseId)
                    .title("Original Title")
                    .availableRooms(3)
                    .build();

            when(boardingHouseService.adminUpdate(eq(houseId), any(UpdateBoardingHouseRequest.class)))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<BoardingHouseResponse> response = controller.adminUpdate(houseId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getAvailableRooms()).isEqualTo(3);
            verify(boardingHouseService, times(1)).adminUpdate(eq(houseId), any(UpdateBoardingHouseRequest.class));
        }
    }

    // ============================================================
    // ADMIN DELETE TESTS
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/v1/admin/boarding-houses/{id} (admin soft delete)")
    class AdminDeleteTests {

        @Test
        @DisplayName("Should soft delete boarding house successfully")
        void adminDelete_success() {
            // Given
            Long houseId = 1L;
            doNothing().when(boardingHouseService).adminDelete(houseId);

            // When
            ApiResponse<Void> response = controller.adminDelete(houseId);

            // Then
            assertThat(response).isNotNull();
            verify(boardingHouseService, times(1)).adminDelete(houseId);
        }

        @Test
        @DisplayName("Should handle soft delete of multiple houses independently")
        void adminDelete_multipleHouses_success() {
            // Given
            Long houseId1 = 1L;
            Long houseId2 = 2L;
            doNothing().when(boardingHouseService).adminDelete(anyLong());

            // When
            ApiResponse<Void> response1 = controller.adminDelete(houseId1);
            ApiResponse<Void> response2 = controller.adminDelete(houseId2);

            // Then
            assertThat(response1).isNotNull();
            assertThat(response2).isNotNull();
            verify(boardingHouseService, times(1)).adminDelete(houseId1);
            verify(boardingHouseService, times(1)).adminDelete(houseId2);
        }
    }

    // ============================================================
    // PERMANENT DELETE TESTS
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/v1/admin/boarding-houses/{id}/permanent (permanent delete)")
    class PermanentlyDeleteTests {

        @Test
        @DisplayName("Should permanently delete boarding house (super admin only)")
        void permanentlyDelete_success() {
            // Given
            Long houseId = 1L;
            doNothing().when(boardingHouseService).permanentlyDelete(houseId);

            // When
            ApiResponse<Void> response = controller.permanentlyDelete(houseId);

            // Then
            assertThat(response).isNotNull();
            verify(boardingHouseService, times(1)).permanentlyDelete(houseId);
        }

        @Test
        @DisplayName("Should handle permanent deletion of multiple houses")
        void permanentlyDelete_multipleHouses_success() {
            // Given
            Long houseId1 = 100L;
            Long houseId2 = 101L;
            Long houseId3 = 102L;
            doNothing().when(boardingHouseService).permanentlyDelete(anyLong());

            // When
            ApiResponse<Void> response1 = controller.permanentlyDelete(houseId1);
            ApiResponse<Void> response2 = controller.permanentlyDelete(houseId2);
            ApiResponse<Void> response3 = controller.permanentlyDelete(houseId3);

            // Then
            assertThat(response1).isNotNull();
            assertThat(response2).isNotNull();
            assertThat(response3).isNotNull();
            verify(boardingHouseService, times(3)).permanentlyDelete(anyLong());
        }
    }

    // ============================================================
    // GET STATS TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/admin/boarding-houses/stats (platform statistics)")
    class GetStatsTests {

        @Test
        @DisplayName("Should return complete platform statistics")
        void getStats_withData_success() {
            // Given
            BoardingHouseStatsResponse statsResponse = BoardingHouseStatsResponse.builder()
                    .totalListings(100L)
                    .availableListings(85L)
                    .unavailableListings(15L)
                    .totalViews(5000L)
                    .maleOnlyListings(20L)
                    .femaleOnlyListings(30L)
                    .anyGenderListings(50L)
                    .build();

            when(boardingHouseService.getStats()).thenReturn(statsResponse);

            // When
            ApiResponse<BoardingHouseStatsResponse> response = controller.getStats();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getTotalListings()).isEqualTo(100L);
            assertThat(response.getData().getAvailableListings()).isEqualTo(85L);
            assertThat(response.getData().getUnavailableListings()).isEqualTo(15L);
            assertThat(response.getData().getTotalViews()).isEqualTo(5000L);
            assertThat(response.getData().getMaleOnlyListings()).isEqualTo(20L);
            assertThat(response.getData().getFemaleOnlyListings()).isEqualTo(30L);
            assertThat(response.getData().getAnyGenderListings()).isEqualTo(50L);
            verify(boardingHouseService, times(1)).getStats();
        }

        @Test
        @DisplayName("Should handle statistics with zero data")
        void getStats_emptyData_success() {
            // Given
            BoardingHouseStatsResponse statsResponse = BoardingHouseStatsResponse.builder()
                    .totalListings(0L)
                    .availableListings(0L)
                    .unavailableListings(0L)
                    .totalViews(0L)
                    .maleOnlyListings(0L)
                    .femaleOnlyListings(0L)
                    .anyGenderListings(0L)
                    .build();

            when(boardingHouseService.getStats()).thenReturn(statsResponse);

            // When
            ApiResponse<BoardingHouseStatsResponse> response = controller.getStats();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getTotalListings()).isEqualTo(0L);
            assertThat(response.getData().getAvailableListings()).isEqualTo(0L);
            verify(boardingHouseService, times(1)).getStats();
        }

        @Test
        @DisplayName("Should handle statistics with large numbers")
        void getStats_largeNumbers_success() {
            // Given
            BoardingHouseStatsResponse statsResponse = BoardingHouseStatsResponse.builder()
                    .totalListings(50000L)
                    .availableListings(45000L)
                    .unavailableListings(5000L)
                    .totalViews(500000L)
                    .maleOnlyListings(10000L)
                    .femaleOnlyListings(15000L)
                    .anyGenderListings(25000L)
                    .build();

            when(boardingHouseService.getStats()).thenReturn(statsResponse);

            // When
            ApiResponse<BoardingHouseStatsResponse> response = controller.getStats();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getTotalListings()).isEqualTo(50000L);
            assertThat(response.getData().getAvailableListings()).isEqualTo(45000L);
            assertThat(response.getData().getTotalViews()).isEqualTo(500000L);
            verify(boardingHouseService, times(1)).getStats();
        }

        @Test
        @DisplayName("Should validate statistics consistency")
        void getStats_validateConsistency() {
            // Given
            BoardingHouseStatsResponse statsResponse = BoardingHouseStatsResponse.builder()
                    .totalListings(100L)
                    .availableListings(60L)
                    .unavailableListings(40L)
                    .totalViews(10000L)
                    .maleOnlyListings(20L)
                    .femaleOnlyListings(30L)
                    .anyGenderListings(50L)
                    .build();

            when(boardingHouseService.getStats()).thenReturn(statsResponse);

            // When
            ApiResponse<BoardingHouseStatsResponse> response = controller.getStats();

            // Then
            assertThat(response.getData().getTotalListings())
                    .isEqualTo(response.getData().getAvailableListings() + response.getData().getUnavailableListings());
            assertThat(response.getData().getTotalListings())
                    .isEqualTo(response.getData().getMaleOnlyListings() 
                            + response.getData().getFemaleOnlyListings() 
                            + response.getData().getAnyGenderListings());
        }
    }
}

