package lk.iit.nextora.module.club.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.request.JoinClubRequest;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.service.ClubService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClubController Unit Tests")
class ClubControllerTest {

    @Mock private ClubService clubService;

    @InjectMocks
    private ClubController controller;

    // ============================================================
    // CREATE CLUB TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /api/v1/clubs (create club)")
    class CreateClubTests {

        @Test
        @DisplayName("Should create club with logo successfully")
        void createClub_withLogo_success() {
            // Given
            MultipartFile logoFile = new MockMultipartFile(
                    "logo", "club_logo.jpg", "image/jpeg", "test image".getBytes()
            );

            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .faculty(FacultyType.COMPUTING)
                    .logoUrl("https://s3.com/clubs/logos/tech_logo.jpg")
                    .email("tech@iit.edu.lk")
                    .isActive(true)
                    .build();

            when(clubService.createClub(any(CreateClubRequest.class), eq(logoFile)))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ClubResponse> response = controller.createClubWithLogo(
                    "Tech Club",
                    "TECH001",
                    "A club for tech enthusiasts",
                    "tech@iit.edu.lk",
                    "0775555555",
                    "COMPUTING",
                    "2023-01-01",
                    100,
                    "https://fb.com/techclub",
                    1L,
                    2L,
                    true,
                    logoFile
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isEqualTo(expectedResponse);
            verify(clubService, times(1)).createClub(any(CreateClubRequest.class), eq(logoFile));
        }

        @Test
        @DisplayName("Should create club without logo successfully")
        void createClub_withoutLogo_success() {
            // Given
            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(2L)
                    .clubCode("ARTS001")
                    .name("Arts Club")
                    .faculty(FacultyType.BUSINESS)
                    .isActive(true)
                    .build();

            when(clubService.createClub(any(CreateClubRequest.class), isNull()))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ClubResponse> response = controller.createClubWithLogo(
                    "Arts Club",
                    "ARTS001",
                    "A club for art lovers",
                    null,
                    null,
                    "BUSINESS",
                    null,
                    50,
                    null,
                    null,
                    null,
                    true,
                    null
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isEqualTo(expectedResponse);
            verify(clubService, times(1)).createClub(any(CreateClubRequest.class), isNull());
        }
    }

    // ============================================================
    // GET CLUB TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/clubs/{clubId} (get club by ID)")
    class GetClubByIdTests {

        @Test
        @DisplayName("Should return club by ID successfully")
        void getClubById_success() {
            // Given
            Long clubId = 1L;
            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(clubId)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .description("Technology club")
                    .faculty(FacultyType.COMPUTING)
                    .build();

            when(clubService.getClubById(clubId))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ClubResponse> response = controller.getClubById(clubId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isEqualTo(expectedResponse);
            verify(clubService, times(1)).getClubById(clubId);
        }

        @Test
        @DisplayName("Should throw exception when club not found")
        void getClubById_notFound() {
            // Given
            Long clubId = 999L;
            when(clubService.getClubById(clubId))
                    .thenThrow(new ResourceNotFoundException("Club", "id", clubId.toString()));

            // When & Then
            assertThatThrownBy(() -> controller.getClubById(clubId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(clubService, times(1)).getClubById(clubId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clubs/code/{clubCode} (get club by code)")
    class GetClubByCodeTests {

        @Test
        @DisplayName("Should return club by code successfully")
        void getClubByCode_success() {
            // Given
            String clubCode = "TECH001";
            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(1L)
                    .clubCode(clubCode)
                    .name("Tech Club")
                    .build();

            when(clubService.getClubByCode(clubCode))
                    .thenReturn(expectedResponse);

            // When
            ApiResponse<ClubResponse> response = controller.getClubByCode(clubCode);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getClubCode()).isEqualTo(clubCode);
            verify(clubService, times(1)).getClubByCode(clubCode);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clubs (get all clubs)")
    class GetAllClubsTests {

        @Test
        @DisplayName("Should return paginated clubs")
        void getAllClubs_returnsPaginatedResult() {
            // Given
            PagedResponse<ClubResponse> pagedResponse = PagedResponse.<ClubResponse>builder()
                    .content(List.of(
                            ClubResponse.builder().id(1L).clubCode("TECH001").name("Tech Club").build(),
                            ClubResponse.builder().id(2L).clubCode("ARTS001").name("Arts Club").build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(clubService.getAllClubs(any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<ClubResponse>> response = controller.getAllClubs(0, 10, "name", "ASC");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getTotalElements()).isEqualTo(2L);
            verify(clubService, times(1)).getAllClubs(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty paginated result")
        void getAllClubs_returnsEmptyResult() {
            // Given
            PagedResponse<ClubResponse> emptyResponse = PagedResponse.<ClubResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();

            when(clubService.getAllClubs(any(Pageable.class)))
                    .thenReturn(emptyResponse);

            // When
            ApiResponse<PagedResponse<ClubResponse>> response = controller.getAllClubs(0, 10, "name", "ASC");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().isEmpty()).isTrue();
            verify(clubService, times(1)).getAllClubs(any(Pageable.class));
        }
    }

    // ============================================================
    // FACULTY FILTER TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/clubs/faculty/{faculty} (get clubs by faculty)")
    class GetClubsByFacultyTests {

        @Test
        @DisplayName("Should return clubs by faculty")
        void getClubsByFaculty_success() {
            // Given
            FacultyType faculty = FacultyType.COMPUTING;
            PagedResponse<ClubResponse> pagedResponse = PagedResponse.<ClubResponse>builder()
                    .content(List.of(
                            ClubResponse.builder().id(1L).clubCode("TECH001").faculty(faculty).build(),
                            ClubResponse.builder().id(2L).clubCode("ROBO001").faculty(faculty).build()
                    ))
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .empty(false)
                    .build();

            when(clubService.getClubsByFaculty(any(FacultyType.class), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // When
            ApiResponse<PagedResponse<ClubResponse>> response = controller.getClubsByFaculty(faculty, 0, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData().getContent()).hasSize(2);
            assertThat(response.getData().getContent()).allMatch(c -> c.getFaculty().equals(faculty));
            verify(clubService, times(1)).getClubsByFaculty(any(FacultyType.class), any(Pageable.class));
        }
    }
}

