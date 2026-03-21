package lk.iit.nextora.module.boardinghouse.entity;

import lk.iit.nextora.common.enums.GenderPreference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BoardingHouse Entity Unit Tests")
class BoardingHouseTest {

    @Nested
    @DisplayName("BoardingHouse Builder")
    class BuilderTests {

        @Test
        @DisplayName("Should create boarding house with all fields")
        void buildBoardingHouse_withAllFields_success() {
            BoardingHouse house = BoardingHouse.builder()
                    .id(1L)
                    .title("Premium House")
                    .description("A spacious boarding house")
                    .price(new BigDecimal("50000"))
                    .address("123 Main Street")
                    .city("Colombo")
                    .district("Western")
                    .genderPreference(GenderPreference.FEMALE)
                    .totalRooms(10)
                    .availableRooms(5)
                    .contactName("John Doe")
                    .contactPhone("0771234567")
                    .build();

            assertThat(house).isNotNull();
            assertThat(house.getId()).isEqualTo(1L);
            assertThat(house.getTitle()).isEqualTo("Premium House");
            assertThat(house.getPrice()).isEqualTo(new BigDecimal("50000"));
            assertThat(house.getCity()).isEqualTo("Colombo");
            assertThat(house.getGenderPreference()).isEqualTo(GenderPreference.FEMALE);
            assertThat(house.getTotalRooms()).isEqualTo(10);
            assertThat(house.getAvailableRooms()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should create boarding house with default values")
        void buildBoardingHouse_withDefaults_success() {
            BoardingHouse house = BoardingHouse.builder()
                    .title("House")
                    .price(new BigDecimal("40000"))
                    .address("Address")
                    .city("City")
                    .district("District")
                    .contactName("Contact")
                    .contactPhone("1234567890")
                    .build();

            assertThat(house).isNotNull();
            assertThat(house.getGenderPreference()).isEqualTo(GenderPreference.ANY);
            assertThat(house.getAvailableRooms()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("BoardingHouse Properties")
    class PropertyTests {

        @Test
        @DisplayName("Should validate required fields")
        void validateRequiredFields() {
            BoardingHouse house = BoardingHouse.builder()
                    .id(1L)
                    .title("House")
                    .price(new BigDecimal("50000"))
                    .address("Address")
                    .city("City")
                    .district("District")
                    .contactName("Contact")
                    .contactPhone("1234567890")
                    .build();

            assertThat(house.getTitle()).isNotNull().isNotEmpty();
            assertThat(house.getPrice()).isNotNull();
            assertThat(house.getAddress()).isNotNull();
            assertThat(house.getCity()).isNotNull();
            assertThat(house.getDistrict()).isNotNull();
        }

        @Test
        @DisplayName("Should support gender preference field")
        void genderPreference_allValues() {
            for (GenderPreference pref : GenderPreference.values()) {
                BoardingHouse house = BoardingHouse.builder()
                        .title("House")
                        .price(new BigDecimal("50000"))
                        .address("Address")
                        .city("City")
                        .district("District")
                        .contactName("Contact")
                        .contactPhone("1234567890")
                        .genderPreference(pref)
                        .build();

                assertThat(house.getGenderPreference()).isEqualTo(pref);
            }
        }

        @Test
        @DisplayName("Should manage available and total rooms")
        void roomManagement() {
            BoardingHouse house = BoardingHouse.builder()
                    .title("House")
                    .price(new BigDecimal("50000"))
                    .address("Address")
                    .city("City")
                    .district("District")
                    .contactName("Contact")
                    .contactPhone("1234567890")
                    .totalRooms(10)
                    .availableRooms(3)
                    .build();

            assertThat(house.getTotalRooms()).isEqualTo(10);
            assertThat(house.getAvailableRooms()).isEqualTo(3);

            int bookedRooms = house.getTotalRooms() - house.getAvailableRooms();
            assertThat(bookedRooms).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("BoardingHouse Setters")
    class SetterTests {

        @Test
        @DisplayName("Should update boarding house properties")
        void updateProperties() {
            BoardingHouse house = BoardingHouse.builder()
                    .title("Old Title")
                    .price(new BigDecimal("50000"))
                    .address("Address")
                    .city("City")
                    .district("District")
                    .contactName("Contact")
                    .contactPhone("1234567890")
                    .build();

            house.setTitle("New Title");
            house.setPrice(new BigDecimal("55000"));
            house.setAvailableRooms(2);

            assertThat(house.getTitle()).isEqualTo("New Title");
            assertThat(house.getPrice()).isEqualTo(new BigDecimal("55000"));
            assertThat(house.getAvailableRooms()).isEqualTo(2);
        }
    }
}

