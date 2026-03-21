package lk.iit.nextora.module.boardinghouse.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.boardinghouse.dto.request.BoardingHouseFilterRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseStatsResponse;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import lk.iit.nextora.module.boardinghouse.mapper.BoardingHouseMapper;
import lk.iit.nextora.module.boardinghouse.repository.BoardingHouseRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardingHouseServiceImpl Unit Tests")
class BoardingHouseServiceImplTest {

    @Mock private BoardingHouseRepository boardingHouseRepository;
    @Mock private BoardingHouseMapper boardingHouseMapper;

    @InjectMocks
    private BoardingHouseServiceImpl boardingHouseService;

    @Nested
    @DisplayName("Create boarding house")
    class CreateBoardingHouseTests {

        @Test
        @DisplayName("Should create boarding house successfully")
        void create_success() {
            CreateBoardingHouseRequest request = CreateBoardingHouseRequest.builder()
                    .title("Premium House").city("Colombo").build();

            BoardingHouse entity = BoardingHouse.builder().id(1L).title("Premium House").build();
            BoardingHouseResponse response = BoardingHouseResponse.builder().id(1L).title("Premium House").build();

            when(boardingHouseRepository.save(any(BoardingHouse.class))).thenReturn(entity);
            when(boardingHouseMapper.toResponse(entity)).thenReturn(response);

            BoardingHouseResponse result = boardingHouseService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(boardingHouseRepository, times(1)).save(any(BoardingHouse.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("Should return boarding house by ID")
        void getById_success() {
            Long houseId = 1L;
            BoardingHouse house = BoardingHouse.builder().id(houseId).title("House 1").build();
            BoardingHouseResponse response = BoardingHouseResponse.builder().id(houseId).title("House 1").build();

            when(boardingHouseRepository.findByIdWithDetails(houseId)).thenReturn(Optional.of(house));
            when(boardingHouseMapper.toResponse(house)).thenReturn(response);

            BoardingHouseResponse result = boardingHouseService.getById(houseId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(houseId);
            verify(boardingHouseRepository, times(1)).findByIdWithDetails(houseId);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getById_notFound_throwsException() {
            Long houseId = 999L;
            when(boardingHouseRepository.findByIdWithDetails(houseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardingHouseService.getById(houseId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllAvailable")
    class GetAllAvailableTests {

        @Test
        @DisplayName("Should return paginated available boarding houses")
        void getAllAvailable_success() {
            Pageable pageable = PageRequest.of(0, 10);
            List<BoardingHouse> houses = List.of(
                    BoardingHouse.builder().id(1L).title("House 1").build(),
                    BoardingHouse.builder().id(2L).title("House 2").build()
            );
            Page<BoardingHouse> page = new PageImpl<>(houses, pageable, 2);

            when(boardingHouseRepository.findByIsDeletedFalseAndIsAvailableTrue(pageable)).thenReturn(page);
            when(boardingHouseMapper.toResponse(houses.get(0))).thenReturn(BoardingHouseResponse.builder().id(1L).title("House 1").build());
            when(boardingHouseMapper.toResponse(houses.get(1))).thenReturn(BoardingHouseResponse.builder().id(2L).title("House 2").build());

            PagedResponse<BoardingHouseResponse> result = boardingHouseService.getAllAvailable(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(boardingHouseRepository, times(1)).findByIsDeletedFalseAndIsAvailableTrue(pageable);
        }
    }

    @Nested
    @DisplayName("filter")
    class FilterTests {

        @Test
        @DisplayName("Should filter boarding houses by criteria")
        void filter_success() {
            BoardingHouseFilterRequest filter = BoardingHouseFilterRequest.builder()
                    .city("Colombo").build();
            Pageable pageable = PageRequest.of(0, 10);

            List<BoardingHouse> filtered = List.of(
                    BoardingHouse.builder().id(1L).title("House 1").city("Colombo").build()
            );
            Page<BoardingHouse> page = new PageImpl<>(filtered, pageable, 1);

            when(boardingHouseRepository.findByCity(eq("Colombo"), eq(pageable)))
                    .thenReturn(page);
            when(boardingHouseMapper.toResponse(filtered.get(0)))
                    .thenReturn(BoardingHouseResponse.builder().id(1L).title("House 1").build());

            PagedResponse<BoardingHouseResponse> result = boardingHouseService.search("Colombo", pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("adminUpdate")
    class AdminUpdateTests {

        @Test
        @DisplayName("Should update boarding house")
        void adminUpdate_success() {
            Long houseId = 1L;
            UpdateBoardingHouseRequest request = UpdateBoardingHouseRequest.builder().title("Updated").build();
            BoardingHouse house = BoardingHouse.builder().id(houseId).title("Updated").build();
            BoardingHouseResponse response = BoardingHouseResponse.builder().id(houseId).title("Updated").build();

            when(boardingHouseRepository.findByIdWithDetails(houseId)).thenReturn(Optional.of(house));
            when(boardingHouseRepository.save(any(BoardingHouse.class))).thenReturn(house);
            when(boardingHouseMapper.toResponse(house)).thenReturn(response);

            BoardingHouseResponse result = boardingHouseService.adminUpdate(houseId, request);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated");
            verify(boardingHouseRepository, times(1)).save(any(BoardingHouse.class));
        }
    }

    @Nested
    @DisplayName("getStats")
    class GetStatsTests {

        @Test
        @DisplayName("Should return boarding house statistics")
        void getStats_success() {
            when(boardingHouseRepository.count()).thenReturn(100L);

            BoardingHouseStatsResponse result = boardingHouseService.getStats();

            assertThat(result).isNotNull();
            verify(boardingHouseRepository, times(1)).count();
        }
    }
}





