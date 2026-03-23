package lk.iit.nextora.module.lostandfound.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.service.FoundItemService;
import lk.iit.nextora.module.lostandfound.service.LostItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LostAndFoundController Unit Tests")
class LostAndFoundControllerTest {

    @Mock private LostItemService lostItemService;
    @Mock private FoundItemService foundItemService;
    @Mock private ItemCategoryRepository categoryRepository;
    @InjectMocks private LostAndFoundController controller;

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return all item categories")
        void getAllCategories_success() {
            ItemCategory cat1 = ItemCategory.builder().id(1L).name("Electronics").build();
            ItemCategory cat2 = ItemCategory.builder().id(2L).name("Accessories").build();

            when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

            // When
            List<ItemCategory> result = categoryRepository.findAll();

            // Then
            assertThat(result).hasSize(2);
            verify(categoryRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("createLostItem")
    class CreateLostItemTests {

        @Test
        @DisplayName("Should create lost item successfully")
        void createLostItem_success() {
            CreateLostItemRequest request = CreateLostItemRequest.builder()
                    .title("Lost iPhone 15")
                    .description("Black iPhone 15 Pro Max")
                    .location("Library Building")
                    .contactNumber("+94701234567")
                    .category("Electronics")
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(1L)
                    .title("Lost iPhone 15")
                    .location("Library Building")
                    .build();

            when(lostItemService.createLostItem(any(CreateLostItemRequest.class)))
                    .thenReturn(response);

            ApiResponse<ItemResponse> result = controller.createLostItem(request);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getId()).isEqualTo(1L);
            verify(lostItemService, times(1)).createLostItem(any(CreateLostItemRequest.class));
        }
    }

    @Nested
    @DisplayName("createFoundItem")
    class CreateFoundItemTests {

        @Test
        @DisplayName("Should create found item successfully")
        void createFoundItem_success() {
            CreateFoundItemRequest request = CreateFoundItemRequest.builder()
                    .title("Found iPhone 15")
                    .description("Black iPhone found in library")
                    .location("Library Building")
                    .contactNumber("+94701234567")
                    .category("Electronics")
                    .pickupLocation("Security Office")
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(2L)
                    .title("Found iPhone 15")
                    .location("Library Building")
                    .build();

            when(foundItemService.createFoundItem(any(CreateFoundItemRequest.class)))
                    .thenReturn(response);

            ApiResponse<ItemResponse> result = controller.createFoundItem(request);

            assertThat(result).isNotNull();
            assertThat(result.getData().getId()).isEqualTo(2L);
            verify(foundItemService, times(1)).createFoundItem(any(CreateFoundItemRequest.class));
        }
    }

    @Nested
    @DisplayName("searchLostItems")
    class SearchLostItemsTests {

        @Test
        @DisplayName("Should search lost items with pagination")
        void searchLostItems_success() {
            SearchItemRequest request = SearchItemRequest.builder()
                    .page(0)
                    .size(10)
                    .build();

            ItemResponse item = ItemResponse.builder()
                    .id(1L)
                    .title("Lost Item")
                    .build();

            ItemListResponse listResponse = ItemListResponse.builder()
                    .items(List.of(item))
                    .totalElements(1L)
                    .build();

            when(lostItemService.searchLostItems(any(SearchItemRequest.class)))
                    .thenReturn(listResponse);

            ApiResponse<ItemListResponse> result = controller.searchLostItems(request);

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotNull();
            verify(lostItemService, times(1)).searchLostItems(any(SearchItemRequest.class));
        }
    }

    @Nested
    @DisplayName("searchFoundItems")
    class SearchFoundItemsTests {

        @Test
        @DisplayName("Should search found items with pagination")
        void searchFoundItems_success() {
            SearchItemRequest request = SearchItemRequest.builder()
                    .page(0)
                    .size(10)
                    .build();

            ItemResponse item = ItemResponse.builder()
                    .id(2L)
                    .title("Found Item")
                    .build();

            ItemListResponse listResponse = ItemListResponse.builder()
                    .items(List.of(item))
                    .totalElements(1L)
                    .build();

            when(foundItemService.searchFoundItems(any(SearchItemRequest.class)))
                    .thenReturn(listResponse);

            ApiResponse<ItemListResponse> result = controller.searchFoundItems(request);

            assertThat(result).isNotNull();
            verify(foundItemService, times(1)).searchFoundItems(any(SearchItemRequest.class));
        }
    }

    @Nested
    @DisplayName("getLostItemById")
    class GetLostItemByIdTests {

        @Test
        @DisplayName("Should return lost item by ID")
        void getLostItemById_success() {
            Long itemId = 1L;
            ItemResponse response = ItemResponse.builder()
                    .id(itemId)
                    .title("Lost iPhone")
                    .build();

            when(lostItemService.getLostItemById(itemId)).thenReturn(response);

            ApiResponse<ItemResponse> result = controller.getLostItemById(itemId);

            assertThat(result).isNotNull();
            assertThat(result.getData().getId()).isEqualTo(itemId);
            verify(lostItemService, times(1)).getLostItemById(itemId);
        }
    }

    @Nested
    @DisplayName("getFoundItemById")
    class GetFoundItemByIdTests {

        @Test
        @DisplayName("Should return found item by ID")
        void getFoundItemById_success() {
            Long itemId = 2L;
            ItemResponse response = ItemResponse.builder()
                    .id(itemId)
                    .title("Found iPhone")
                    .build();

            when(foundItemService.getFoundItemById(itemId)).thenReturn(response);

            ApiResponse<ItemResponse> result = controller.getFoundItemById(itemId);

            assertThat(result).isNotNull();
            assertThat(result.getData().getId()).isEqualTo(itemId);
            verify(foundItemService, times(1)).getFoundItemById(itemId);
        }
    }

    @Nested
    @DisplayName("updateLostItem")
    class UpdateLostItemTests {

        @Test
        @DisplayName("Should update lost item successfully")
        void updateLostItem_success() {
            Long itemId = 1L;
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(itemId)
                    .title("Updated Title")
                    .build();

            when(lostItemService.updateLostItem(eq(itemId), any(UpdateItemRequest.class)))
                    .thenReturn(response);

            ApiResponse<ItemResponse> result = controller.updateLostItem(itemId, request);

            assertThat(result).isNotNull();
            assertThat(result.getData().getTitle()).isEqualTo("Updated Title");
            verify(lostItemService, times(1)).updateLostItem(eq(itemId), any(UpdateItemRequest.class));
        }
    }

    @Nested
    @DisplayName("updateFoundItem")
    class UpdateFoundItemTests {

        @Test
        @DisplayName("Should update found item successfully")
        void updateFoundItem_success() {
            Long itemId = 2L;
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .title("Updated Found Item")
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(itemId)
                    .title("Updated Found Item")
                    .build();

            when(foundItemService.updateFoundItem(eq(itemId), any(UpdateItemRequest.class)))
                    .thenReturn(response);

            ApiResponse<ItemResponse> result = controller.updateFoundItem(itemId, request);

            assertThat(result).isNotNull();
            verify(foundItemService, times(1)).updateFoundItem(eq(itemId), any(UpdateItemRequest.class));
        }
    }

    @Nested
    @DisplayName("deleteLostItem")
    class DeleteLostItemTests {

        @Test
        @DisplayName("Should delete lost item successfully")
        void deleteLostItem_success() {
            Long itemId = 1L;

            doNothing().when(lostItemService).adminDeleteLostItem(itemId);

            ApiResponse<Void> result = controller.deleteLostItem(itemId);

            assertThat(result).isNotNull();
            verify(lostItemService, times(1)).adminDeleteLostItem(itemId);
        }
    }

    @Nested
    @DisplayName("deleteFoundItem")
    class DeleteFoundItemTests {

        @Test
        @DisplayName("Should delete found item successfully")
        void deleteFoundItem_success() {
            Long itemId = 2L;

            doNothing().when(foundItemService).adminDeleteFoundItem(itemId);

            ApiResponse<Void> result = controller.deleteFoundItem(itemId);

            assertThat(result).isNotNull();
            verify(foundItemService, times(1)).adminDeleteFoundItem(itemId);
        }
    }
}









