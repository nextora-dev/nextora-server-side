package lk.iit.nextora.module.club.service.impl;

import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.exception.custom.DuplicateResourceException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.mapper.ClubMapper;
import lk.iit.nextora.module.club.repository.ClubAnnouncementRepository;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.club.service.ClubActivityLogService;
import lk.iit.nextora.module.election.repository.ElectionRepository;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClubServiceImpl Unit Tests")
class ClubServiceImplTest {

    @Mock private ClubRepository clubRepository;
    @Mock private ClubMembershipRepository membershipRepository;
    @Mock private ClubAnnouncementRepository announcementRepository;
    @Mock private ElectionRepository electionRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AcademicStaffRepository academicStaffRepository;
    @Mock private SecurityService securityService;
    @Mock private ClubMapper clubMapper;
    @Mock private ClubActivityLogService activityLogService;
    @Mock private S3Service s3Service;
    @Mock private PushNotificationService pushNotificationService;

    @InjectMocks
    private ClubServiceImpl clubService;

    // ============================================================
    // CREATE CLUB TESTS
    // ============================================================

    @Nested
    @DisplayName("createClub")
    class CreateClubTests {

        @Test
        @DisplayName("Should create club with logo successfully")
        void createClub_withLogo_success() {
            // Given
            CreateClubRequest request = CreateClubRequest.builder()
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .description("A club for tech enthusiasts")
                    .faculty(FacultyType.COMPUTING)
                    .email("tech@iit.edu.lk")
                    .contactNumber("0775555555")
                    .maxMembers(100)
                    .isRegistrationOpen(true)
                    .build();

            MultipartFile logo = new MockMultipartFile(
                    "logo", "tech_logo.jpg", "image/jpeg", "test image".getBytes()
            );

            Club clubEntity = Club.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .description("A club for tech enthusiasts")
                    .faculty(FacultyType.COMPUTING)
                    .email("tech@iit.edu.lk")
                    .build();

            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(1L)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .description("A club for tech enthusiasts")
                    .faculty(FacultyType.COMPUTING)
                    .logoUrl("https://s3.com/clubs/logos/tech_logo.jpg")
                    .build();

            when(clubRepository.existsByClubCodeAndIsDeletedFalse("TECH001")).thenReturn(false);
            when(clubRepository.existsByNameAndIsDeletedFalse("Tech Club")).thenReturn(false);
            when(clubMapper.toEntity(any(CreateClubRequest.class))).thenReturn(clubEntity);
            when(s3Service.uploadFilePublic(logo, "clubs/logos")).thenReturn("https://s3.com/clubs/logos/tech_logo.jpg");
            when(clubRepository.save(any(Club.class))).thenReturn(clubEntity);
            when(clubMapper.toResponse(clubEntity)).thenReturn(expectedResponse);

            // When
            ClubResponse response = clubService.createClub(request, logo);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getClubCode()).isEqualTo("TECH001");
            verify(clubRepository, times(1)).existsByClubCodeAndIsDeletedFalse("TECH001");
            verify(clubRepository, times(1)).save(any(Club.class));
        }

        @Test
        @DisplayName("Should create club without logo successfully")
        void createClub_withoutLogo_success() {
            // Given
            CreateClubRequest request = CreateClubRequest.builder()
                    .clubCode("ARTS001")
                    .name("Arts Club")
                    .description("A club for art lovers")
                    .build();

            Club clubEntity = Club.builder()
                    .id(2L)
                    .clubCode("ARTS001")
                    .name("Arts Club")
                    .description("A club for art lovers")
                    .build();

            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(2L)
                    .clubCode("ARTS001")
                    .name("Arts Club")
                    .build();

            when(clubRepository.existsByClubCodeAndIsDeletedFalse("ARTS001")).thenReturn(false);
            when(clubRepository.existsByNameAndIsDeletedFalse("Arts Club")).thenReturn(false);
            when(clubMapper.toEntity(request)).thenReturn(clubEntity);
            when(clubRepository.save(clubEntity)).thenReturn(clubEntity);
            when(clubMapper.toResponse(clubEntity)).thenReturn(expectedResponse);

            // When
            ClubResponse response = clubService.createClub(request, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
            verify(clubRepository, times(1)).save(clubEntity);
            verify(s3Service, never()).uploadFilePublic(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when club code already exists")
        void createClub_duplicateCode_throwsException() {
            // Given
            CreateClubRequest request = CreateClubRequest.builder()
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .build();

            when(clubRepository.existsByClubCodeAndIsDeletedFalse("TECH001"))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> clubService.createClub(request, null))
                    .isInstanceOf(DuplicateResourceException.class);
            verify(clubRepository, times(1)).existsByClubCodeAndIsDeletedFalse("TECH001");
        }

        @Test
        @DisplayName("Should throw exception when club name already exists")
        void createClub_duplicateName_throwsException() {
            // Given
            CreateClubRequest request = CreateClubRequest.builder()
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .build();

            when(clubRepository.existsByClubCodeAndIsDeletedFalse("TECH001")).thenReturn(false);
            when(clubRepository.existsByNameAndIsDeletedFalse("Tech Club")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> clubService.createClub(request, null))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    // ============================================================
    // GET CLUB TESTS
    // ============================================================

    @Nested
    @DisplayName("getClubById")
    class GetClubByIdTests {

        @Test
        @DisplayName("Should return club by ID successfully")
        void getClubById_success() {
            // Given
            Long clubId = 1L;
            Club club = Club.builder()
                    .id(clubId)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .build();

            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(clubId)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .build();

            when(clubRepository.findByIdAndIsDeletedFalse(clubId)).thenReturn(Optional.of(club));
            when(clubMapper.toResponse(club)).thenReturn(expectedResponse);

            // When
            ClubResponse response = clubService.getClubById(clubId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(clubId);
            verify(clubRepository, times(1)).findByIdAndIsDeletedFalse(clubId);
        }

        @Test
        @DisplayName("Should throw exception when club not found")
        void getClubById_notFound_throwsException() {
            // Given
            Long clubId = 999L;
            when(clubRepository.findByIdAndIsDeletedFalse(clubId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> clubService.getClubById(clubId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(clubRepository, times(1)).findByIdAndIsDeletedFalse(clubId);
        }
    }

    @Nested
    @DisplayName("getClubByCode")
    class GetClubByCodeTests {

        @Test
        @DisplayName("Should return club by code successfully")
        void getClubByCode_success() {
            // Given
            String clubCode = "TECH001";
            Club club = Club.builder()
                    .id(1L)
                    .clubCode(clubCode)
                    .name("Tech Club")
                    .build();

            ClubResponse expectedResponse = ClubResponse.builder()
                    .id(1L)
                    .clubCode(clubCode)
                    .name("Tech Club")
                    .build();

            when(clubRepository.findByClubCodeAndIsDeletedFalse(clubCode)).thenReturn(Optional.of(club));
            when(clubMapper.toResponse(club)).thenReturn(expectedResponse);

            // When
            ClubResponse response = clubService.getClubByCode(clubCode);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getClubCode()).isEqualTo(clubCode);
            verify(clubRepository, times(1)).findByClubCodeAndIsDeletedFalse(clubCode);
        }

        @Test
        @DisplayName("Should throw exception when club code not found")
        void getClubByCode_notFound_throwsException() {
            // Given
            String clubCode = "INVALID001";
            when(clubRepository.findByClubCodeAndIsDeletedFalse(clubCode)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> clubService.getClubByCode(clubCode))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // LIST CLUBS TESTS
    // ============================================================

    @Nested
    @DisplayName("getAllClubs")
    class GetAllClubsTests {

        @Test
        @DisplayName("Should return paginated clubs")
        void getAllClubs_returnsPaginatedResult() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Club> clubs = List.of(
                    Club.builder().id(1L).clubCode("TECH001").name("Tech Club").build(),
                    Club.builder().id(2L).clubCode("ARTS001").name("Arts Club").build()
            );
            Page<Club> clubPage = new PageImpl<>(clubs, pageable, 2);

            List<ClubResponse> responses = List.of(
                    ClubResponse.builder().id(1L).clubCode("TECH001").name("Tech Club").build(),
                    ClubResponse.builder().id(2L).clubCode("ARTS001").name("Arts Club").build()
            );

            when(clubRepository.findByIsDeletedFalseAndIsActiveTrue(pageable)).thenReturn(clubPage);
            when(clubMapper.toResponse(clubs.get(0))).thenReturn(responses.get(0));
            when(clubMapper.toResponse(clubs.get(1))).thenReturn(responses.get(1));

            // When
            var response = clubService.getAllClubs(pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            verify(clubRepository, times(1)).findByIsDeletedFalseAndIsActiveTrue(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no clubs exist")
        void getAllClubs_returnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Club> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(clubRepository.findByIsDeletedFalseAndIsActiveTrue(pageable)).thenReturn(emptyPage);

            // When
            var response = clubService.getAllClubs(pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
        }
    }
}

