package lk.iit.nextora.module.boardinghouse.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.GenderPreference;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.boardinghouse.dto.request.BoardingHouseFilterRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardingHouseController Unit Tests")
class BoardingHouseControllerTest {

    @Mock private BoardingHouseService boardingHouseService;

    @InjectMocks
    private BoardingHouseController controller;

    // ============================================================
    // GET ALL AVAILABLE TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/boarding-houses (get all available)")
    class GetAllAvailableTests {

        @Test
        @DisplayName("Should return paginated available boarding houses")
        void getAllAvailable_returnsPaginatedResult() {
            // Given
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            Pageable pageable = PageRequest.of(0, 10, sort);
            
            List<BoardingHouseResponse> houses = List.of(
                    BoardingHouseResponse.builder().id(1L).title("House 1").city("Colombo").price(new BigDecimal("50000")).build(),
                    BoardingHouseResponse.builder().id(2L).title("House 2").city("Kandy").price(new BigDecimal("40000")).build()
            );
            
            PagedResponse<BoardingHouseResponse> pagedResponse = PagedResponse.<BoardingHouseResponse>builder()
                    .content(houses)
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(boardingHouseService.getAllAvailable(any(Pageable.class))).thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<BoardingHouseResponse>> response = controller.getAllAvailable(0, 10, "createdAt", "DESC");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getTotalElements()).isEqualTo(2L);
            verify(boardingHouseService, times(1)).getAllAvailable(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no houses available")
        void getAllAvailable_returnsEmptyResult() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            PagedResponse<BoardingHouseResponse> emptyResponse = PagedResponse.<BoardingHouseResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();

            when(boardingHouseService.getAllAvailable(any(Pageable.class))).thenReturn(emptyResponse);

            // When
            ApiResponse<PagedResponse<BoardingHouseResponse>> response = controller.getAllAvailable(0, 10, "createdAt", "DESC");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().isEmpty()).isTrue();
        }
    }

    // ============================================================
    // GET BY ID TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/boarding-houses/{id} (get by ID)")
    class GetByIdTests {

        @Test
        @DisplayName("Should return boarding house by ID successfully")
        void getById_success() {
            // Given
            Long houseId = 1L;
            BoardingHouseResponse expectedResponse = BoardingHouseResponse.builder()
                    .id(houseId)
                    .title("Premium Boarding House")
                    .city("Colombo")
                    .price(new BigDecimal("50000"))
                    .availableRooms(5)
                    .genderPreference(GenderPreference.FEMALE)
                    .build();

            when(boardingHouseService.getById(houseId)).thenReturn(expectedResponse);

            // When
            ApiResponse<BoardingHouseResponse> response = controller.getById(houseId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getId()).isEqualTo(houseId);
            assertThat(response.getData().getTitle()).isEqualTo("Premium Boarding House");
            verify(boardingHouseService, times(1)).getById(houseId);
        }

        @Test
        @DisplayName("Should throw exception when boarding house not found")
        void getById_notFound_throwsException() {
            // Given
            Long houseId = 999L;
            when(boardingHouseService.getById(houseId))
                    .thenThrow(new ResourceNotFoundException("Boarding House", "id", houseId.toString()));

            // When & Then
            assertThatThrownBy(() -> controller.getById(houseId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // FILTER TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/boarding-houses/filter (filter houses)")
    class FilterTests {

        @Test
        @DisplayName("Should filter boarding houses by criteria")
        void filterBoardingHouses_success() {
            // Given
            BoardingHouseFilterRequest filterRequest = BoardingHouseFilterRequest.builder()
                    .city("Colombo")
                    .minPrice(new BigDecimal("30000"))
                    .maxPrice(new BigDecimal("60000"))
                    .genderPreference(GenderPreference.FEMALE)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            List<BoardingHouseResponse> filteredHouses = List.of(
                    BoardingHouseResponse.builder().id(1L).title("House 1").city("Colombo").price(new BigDecimal("45000")).build()
            );
            
            PagedResponse<BoardingHouseResponse> pagedResponse = PagedResponse.<BoardingHouseResponse>builder()
                    .content(filteredHouses)
                    .totalElements(1L)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            when(boardingHouseService.filterBoardingHouses(eq(filterRequest), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<BoardingHouseResponse>> response = controller.filterBoardingHouses(filterRequest, 0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(1);
            verify(boardingHouseService, times(1)).filterBoardingHouses(eq(filterRequest), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty result when no houses match filter criteria")
        void filterBoardingHouses_noMatch() {
            // Given
            BoardingHouseFilterRequest filterRequest = BoardingHouseFilterRequest.builder()
                    .city("NonExistent")
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            PagedResponse<BoardingHouseResponse> emptyResponse = PagedResponse.<BoardingHouseResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .build();

            when(boardingHouseService.filterBoardingHouses(eq(filterRequest), any(Pageable.class)))
                    .thenReturn(emptyResponse);

            // When
            ApiResponse<PagedResponse<BoardingHouseResponse>> response = controller.filterBoardingHouses(filterRequest, 0, 10);

            // Then
            assertThat(response.getData().isEmpty()).isTrue();
        }
    }

    // ============================================================
    // GET FAVORITES TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/boarding-houses/favorites (get favorites)")
    class GetFavoritesTests {

        @Test
        @DisplayName("Should return user's favorite boarding houses")
        void getFavorites_success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<BoardingHouseResponse> favorites = List.of(
                    BoardingHouseResponse.builder().id(1L).title("Favorite 1").build(),
                    BoardingHouseResponse.builder().id(2L).title("Favorite 2").build()
            );
            
            PagedResponse<BoardingHouseResponse> pagedResponse = PagedResponse.<BoardingHouseResponse>builder()
                    .content(favorites)
                    .totalElements(2L)
                    .build();

            when(boardingHouseService.getFavorites(any(Pageable.class))).thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<BoardingHouseResponse>> response = controller.getFavorites(0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            verify(boardingHouseService, times(1)).getFavorites(any(Pageable.class));
        }
    }

    // ============================================================
    // ADD/REMOVE FAVORITE TESTS
    // ============================================================

    @Nested
    @DisplayName("POST/DELETE /api/v1/boarding-houses/{id}/favorite (favorite operations)")
    class FavoriteOperationTests {

        @Test
        @DisplayName("Should add boarding house to favorites")
        void addFavorite_success() {
            // Given
            Long houseId = 1L;
            doNothing().when(boardingHouseService).addFavorite(houseId);

            // When
            ApiResponse<Void> response = controller.addFavorite(houseId);

            // Then
            assertThat(response).isNotNull();
            verify(boardingHouseService, times(1)).addFavorite(houseId);
        }

        @Test
        @DisplayName("Should remove boarding house from favorites")
        void removeFavorite_success() {
            // Given
            Long houseId = 1L;
            doNothing().when(boardingHouseService).removeFavorite(houseId);

            // When
            ApiResponse<Void> response = controller.removeFavorite(houseId);

            // Then
            assertThat(response).isNotNull();
            verify(boardingHouseService, times(1)).removeFavorite(houseId);
        }
    }

    // ============================================================
    // SEARCH TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/boarding-houses/search (search)")
    class SearchTests {

        @Test
        @DisplayName("Should search boarding houses by keyword")
        void searchBoardingHouses_success() {
            // Given
            String keyword = "premium";
            Pageable pageable = PageRequest.of(0, 10);
            List<BoardingHouseResponse> searchResults = List.of(
                    BoardingHouseResponse.builder().id(1L).title("Premium House").build()
            );
            
            PagedResponse<BoardingHouseResponse> pagedResponse = PagedResponse.<BoardingHouseResponse>builder()
                    .content(searchResults)
                    .totalElements(1L)
                    .build();

            when(boardingHouseService.searchBoardingHouses(eq(keyword), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<BoardingHouseResponse>> response = controller.searchBoardingHouses(keyword, 0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(1);
            verify(boardingHouseService, times(1)).searchBoardingHouses(eq(keyword), any(Pageable.class));
        }
    }
}

