package lk.iit.nextora.module.lostandfound.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LostItem Entity Unit Tests")
class LostItemTest {

    // ============================================================
    // CONSTRUCTION AND INITIALIZATION TESTS
    // ============================================================

    @Nested
    @DisplayName("Builder and Initialization")
    class BuilderTests {

        @Test
        @DisplayName("Should create LostItem with all required fields")
        void builder_withAllFields_createsValidItem() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            String title = "Lost iPhone 15";
            String description = "Black iPhone 15 Pro Max";
            String location = "Library Building";
            String contactNumber = "+94701234567";
            Long reportedBy = 1L;
            String reporterName = "John Doe";
            LocalDateTime dateLost = now.minusDays(1);

            // When
            LostItem item = LostItem.builder()
                    .title(title)
                    .description(description)
                    .location(location)
                    .contactNumber(contactNumber)
                    .reportedBy(reportedBy)
                    .reporterName(reporterName)
                    .dateLost(dateLost)
                    .build();

            // Then
            assertThat(item).isNotNull();
            assertThat(item.getTitle()).isEqualTo(title);
            assertThat(item.getDescription()).isEqualTo(description);
            assertThat(item.getLocation()).isEqualTo(location);
            assertThat(item.getContactNumber()).isEqualTo(contactNumber);
            assertThat(item.getReportedBy()).isEqualTo(reportedBy);
            assertThat(item.getReporterName()).isEqualTo(reporterName);
            assertThat(item.getDateLost()).isEqualTo(dateLost);
            assertThat(item.isActive()).isTrue(); // Default value
        }

        @Test
        @DisplayName("Should set active status to true by default")
        void builder_defaultActiveStatus_isTrue() {
            // Given & When
            LostItem item = LostItem.builder()
                    .title("Lost Wallet")
                    .location("Campus")
                    .contactNumber("+94701234567")
                    .build();

            // Then
            assertThat(item.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should allow setting custom active status")
        void builder_customActiveStatus_isSet() {
            // Given & When
            LostItem item = LostItem.builder()
                    .title("Lost Wallet")
                    .location("Campus")
                    .contactNumber("+94701234567")
                    .active(false)
                    .build();

            // Then
            assertThat(item.isActive()).isFalse();
        }
    }

    // ============================================================
    // TIMESTAMP TESTS
    // ============================================================

    @Nested
    @DisplayName("Timestamp Management")
    class TimestampTests {

        @Test
        @DisplayName("Should set createdAt and updatedAt on creation")
        void onCreate_setsTimestamps() {
            // Given
            LostItem item = LostItem.builder()
                    .title("Lost Keychain")
                    .location("Cafeteria")
                    .contactNumber("+94701234567")
                    .build();

            // When
            item.onCreate();

            // Then
            assertThat(item.getCreatedAt()).isNotNull();
            assertThat(item.getUpdatedAt()).isNotNull();
            assertThat(item.getCreatedAt()).isEqualTo(item.getUpdatedAt());
        }

        @Test
        @DisplayName("Should update only updatedAt on update")
        void onUpdate_updatesTimestamp() {
            // Given
            LostItem item = LostItem.builder()
                    .title("Lost Keychain")
                    .location("Cafeteria")
                    .contactNumber("+94701234567")
                    .build();

            item.onCreate();
            LocalDateTime originalCreatedAt = item.getCreatedAt();
            
            // When - Simulate delay
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            item.onUpdate();

            // Then
            assertThat(item.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(item.getUpdatedAt()).isAfter(originalCreatedAt);
        }
    }

    // ============================================================
    // FIELD VALIDATION TESTS
    // ============================================================

    @Nested
    @DisplayName("Field Validation")
    class FieldValidationTests {

        @Test
        @DisplayName("Should accept valid contact number format")
        void contactNumber_validFormat_accepted() {
            // Given
            String validPhone = "+94701234567";

            // When
            LostItem item = LostItem.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber(validPhone)
                    .build();

            // Then
            assertThat(item.getContactNumber()).isEqualTo(validPhone);
        }

        @Test
        @DisplayName("Should store description up to 1000 characters")
        void description_longText_accepted() {
            // Given
            String longDescription = "A".repeat(1000);

            // When
            LostItem item = LostItem.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .description(longDescription)
                    .build();

            // Then
            assertThat(item.getDescription()).hasSize(1000);
        }

        @Test
        @DisplayName("Should accept null optional fields")
        void optionalFields_null_accepted() {
            // Given & When
            LostItem item = LostItem.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .description(null)
                    .reportedBy(null)
                    .reporterName(null)
                    .dateLost(null)
                    .build();

            // Then
            assertThat(item.getDescription()).isNull();
            assertThat(item.getReportedBy()).isNull();
            assertThat(item.getReporterName()).isNull();
            assertThat(item.getDateLost()).isNull();
        }
    }

    // ============================================================
    // CATEGORY ASSOCIATION TESTS
    // ============================================================

    @Nested
    @DisplayName("Category Association")
    class CategoryTests {

        @Test
        @DisplayName("Should associate with ItemCategory")
        void category_association_works() {
            // Given
            ItemCategory category = ItemCategory.builder()
                    .id(1L)
                    .name("Electronics")
                    .build();

            // When
            LostItem item = LostItem.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .category(category)
                    .build();

            // Then
            assertThat(item.getCategory()).isNotNull();
            assertThat(item.getCategory().getId()).isEqualTo(1L);
            assertThat(item.getCategory().getName()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Should allow null category")
        void category_null_accepted() {
            // Given & When
            LostItem item = LostItem.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .category(null)
                    .build();

            // Then
            assertThat(item.getCategory()).isNull();
        }
    }

    // ============================================================
    // EQUALITY AND IDENTITY TESTS
    // ============================================================

    @Nested
    @DisplayName("Object Equality")
    class EqualityTests {

        @Test
        @DisplayName("Should have same id when created from same builder")
        void equality_sameId_equal() {
            // Given
            Long itemId = 1L;
            LostItem item1 = LostItem.builder()
                    .id(itemId)
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .build();

            LostItem item2 = LostItem.builder()
                    .id(itemId)
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .build();

            // When & Then
            assertThat(item1.getId()).isEqualTo(item2.getId());
        }

        @Test
        @DisplayName("Should be distinct objects with different IDs")
        void equality_differentIds_notEqual() {
            // Given
            LostItem item1 = LostItem.builder()
                    .id(1L)
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .build();

            LostItem item2 = LostItem.builder()
                    .id(2L)
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .build();

            // When & Then
            assertThat(item1.getId()).isNotEqualTo(item2.getId());
        }
    }

    // ============================================================
    // GETTERS AND SETTERS TESTS
    // ============================================================

    @Nested
    @DisplayName("Getters and Setters")
    class GettersSettersTests {

        @Test
        @DisplayName("Should update title via setter")
        void setter_title_updated() {
            // Given
            LostItem item = LostItem.builder()
                    .title("Original Title")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .build();

            // When
            String newTitle = "Updated Title";
            item.setTitle(newTitle);

            // Then
            assertThat(item.getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("Should toggle active status via setter")
        void setter_active_toggled() {
            // Given
            LostItem item = LostItem.builder()
                    .title("Lost Item")
                    .location("Somewhere")
                    .contactNumber("+94701234567")
                    .build();

            // When
            item.setActive(false);

            // Then
            assertThat(item.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should update all mutable fields")
        void setters_multipleFields_updated() {
            // Given
            LostItem item = LostItem.builder()
                    .title("Original Title")
                    .description("Original Description")
                    .location("Original Location")
                    .contactNumber("+94701234567")
                    .build();

            // When
            item.setTitle("New Title");
            item.setDescription("New Description");
            item.setLocation("New Location");
            item.setContactNumber("+94709999999");

            // Then
            assertThat(item.getTitle()).isEqualTo("New Title");
            assertThat(item.getDescription()).isEqualTo("New Description");
            assertThat(item.getLocation()).isEqualTo("New Location");
            assertThat(item.getContactNumber()).isEqualTo("+94709999999");
        }
    }
}

