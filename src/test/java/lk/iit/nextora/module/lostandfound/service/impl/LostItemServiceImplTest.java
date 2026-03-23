package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.entity.LostItem;
import lk.iit.nextora.module.lostandfound.mapper.LostItemMapper;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LostItemServiceImpl Unit Tests")
class LostItemServiceImplTest {

    @Mock
    private LostItemRepository lostItemRepository;

    @Mock
    private ItemCategoryRepository categoryRepository;

    @Mock
    private LostItemMapper lostItemMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private LostItemServiceImpl lostItemService;

    // ============================================================
    // CREATE LOST ITEM TESTS
    // ============================================================

    @Nested
    @DisplayName("createLostItem")
    class CreateLostItemTests {

        @Test
        @DisplayName("Should create lost item successfully")
        void createLostItem_success() {
            // Given
            Long userId = 1L;
            CreateLostItemRequest request = CreateLostItemRequest.builder()
                    .title("Lost iPhone 15")
                    .description("Black iPhone 15 Pro Max")
                    .location("Library Building")
                    .contactNumber("+94701234567")
                    .category("Electronics")
                    .dateLost(LocalDateTime.now().minusDays(1))
                    .build();

            Student user = new Student();
            user.setId(userId);
            user.setFirstName("John");
            user.setLastName("Doe");

            ItemCategory category = ItemCategory.builder()
                    .id(1L)
                    .name("Electronics")
                    .build();

            LostItem entity = LostItem.builder()
                    .id(1L)
                    .title("Lost iPhone 15")
                    .active(true)
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(1L)
                    .title("Lost iPhone 15")
                    .build();

            when(securityService.getCurrentUser()).thenReturn(Optional.of(user));
            when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));
            when(lostItemMapper.toEntity(request)).thenReturn(entity);
            when(lostItemRepository.save(any(LostItem.class))).thenReturn(entity);
            when(lostItemMapper.toResponse(entity)).thenReturn(response);

            // When
            ItemResponse result = lostItemService.createLostItem(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Lost iPhone 15");
            verify(securityService, times(1)).getCurrentUser();
            verify(lostItemRepository, times(1)).save(any(LostItem.class));
        }

        @Test
        @DisplayName("Should throw exception when user not authenticated")
        void createLostItem_userNotAuthenticated_throws() {
            // Given
            CreateLostItemRequest request = CreateLostItemRequest.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .category("Electronics")
                    .build();

            when(securityService.getCurrentUser()).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> lostItemService.createLostItem(request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void createLostItem_categoryNotFound_throws() {
            // Given
            Student user = new Student();
            user.setId(1L);
            user.setFirstName("John");
            user.setLastName("Doe");

            CreateLostItemRequest request = CreateLostItemRequest.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .category("NonexistentCategory")
                    .build();

            when(securityService.getCurrentUser()).thenReturn(Optional.of(user));
            when(categoryRepository.findByNameIgnoreCase("NonexistentCategory")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> lostItemService.createLostItem(request))
                    .isInstanceOf(Exception.class);
        }
    }

    // ============================================================
    // UPDATE LOST ITEM TESTS
    // ============================================================

    @Nested
    @DisplayName("updateLostItem")
    class UpdateLostItemTests {

        @Test
        @DisplayName("Should update lost item successfully")
        void updateLostItem_success() {
            // Given
            Long itemId = 1L;
            Long userId = 1L;

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .title("Updated Lost Item")
                    .description("Updated Description")
                    .build();

            LostItem item = LostItem.builder()
                    .id(itemId)
                    .title("Original Title")
                    .reportedBy(userId)
                    .active(true)
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(itemId)
                    .title("Updated Lost Item")
                    .build();

            when(lostItemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(securityService.getCurrentUserId()).thenReturn(userId);
            when(securityService.isAdmin()).thenReturn(false);
            when(lostItemRepository.save(any(LostItem.class))).thenReturn(item);
            when(lostItemMapper.toResponse(item)).thenReturn(response);

            // When
            ItemResponse result = lostItemService.updateLostItem(itemId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(itemId);
            verify(lostItemRepository, times(1)).save(any(LostItem.class));
        }

        @Test
        @DisplayName("Should throw exception when user not owner and not admin")
        void updateLostItem_notOwnerAndNotAdmin_throws() {
            Long itemId = 1L;
            Long userId = 2L;
            Long ownerUserId = 1L;

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .title("Updated Title")
                    .build();

            LostItem item = LostItem.builder()
                    .id(itemId)
                    .title("Original Title")
                    .reportedBy(ownerUserId)
                    .build();

            when(lostItemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(securityService.getCurrentUserId()).thenReturn(userId);
            when(securityService.isAdmin()).thenReturn(false);

            assertThatThrownBy(() -> lostItemService.updateLostItem(itemId, request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should allow admin to update any item")
        void updateLostItem_adminCanUpdate() {
            // Given
            Long itemId = 1L;
            Long adminUserId = 999L;
            Long ownerUserId = 1L;

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .title("Updated Title")
                    .build();

            LostItem item = LostItem.builder()
                    .id(itemId)
                    .title("Original Title")
                    .reportedBy(ownerUserId)
                    .active(true)
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(itemId)
                    .title("Updated Title")
                    .build();

            when(lostItemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(securityService.getCurrentUserId()).thenReturn(adminUserId);
            when(securityService.isAdmin()).thenReturn(true);
            when(lostItemRepository.save(any(LostItem.class))).thenReturn(item);
            when(lostItemMapper.toResponse(item)).thenReturn(response);

            // When
            ItemResponse result = lostItemService.updateLostItem(itemId, request);

            // Then
            assertThat(result).isNotNull();
            verify(lostItemRepository, times(1)).save(any(LostItem.class));
        }    }

    // ============================================================
    // GET LOST ITEM BY ID TESTS
    // ============================================================

    @Nested
    @DisplayName("getLostItemById")
    class GetLostItemByIdTests {

        @Test
        @DisplayName("Should return lost item by ID")
        void getLostItemById_success() {
            // Given
            Long itemId = 1L;
            LostItem item = LostItem.builder()
                    .id(itemId)
                    .title("Lost iPhone 15")
                    .active(true)
                    .build();

            ItemResponse response = ItemResponse.builder()
                    .id(itemId)
                    .title("Lost iPhone 15")
                    .build();

            when(lostItemRepository.findByIdWithCategory(itemId)).thenReturn(Optional.of(item));
            when(lostItemMapper.toResponse(item)).thenReturn(response);

            // When
            ItemResponse result = lostItemService.getLostItemById(itemId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(itemId);
            assertThat(result.getTitle()).isEqualTo("Lost iPhone 15");
            verify(lostItemRepository, times(1)).findByIdWithCategory(itemId);
        }

        @Test
        @DisplayName("Should throw exception when item not found")
        void getLostItemById_notFound_throws() {
            // Given
            Long itemId = 999L;
            when(lostItemRepository.findByIdWithCategory(itemId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> lostItemService.getLostItemById(itemId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }


    // ============================================================
    // DELETE LOST ITEM TESTS
    // ============================================================

    @Nested
    @DisplayName("adminDeleteLostItem")
    class DeleteLostItemTests {

        @Test
        @DisplayName("Should delete lost item (soft delete)")
        void adminDeleteLostItem_success() {
            // Given
            Long itemId = 1L;
            LostItem item = LostItem.builder()
                    .id(itemId)
                    .title("Lost Item")
                    .active(true)
                    .build();

            when(lostItemRepository.findById(itemId)).thenReturn(Optional.of(item));
            when(lostItemRepository.save(any(LostItem.class))).thenReturn(item);

            // When
            lostItemService.adminDeleteLostItem(itemId);

            // Then
            verify(lostItemRepository, times(1)).save(any(LostItem.class));
        }

        @Test
        @DisplayName("Should throw exception when item not found")
        void adminDeleteLostItem_notFound_throws() {
            // Given
            Long itemId = 999L;
            when(lostItemRepository.findById(itemId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> lostItemService.adminDeleteLostItem(itemId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

