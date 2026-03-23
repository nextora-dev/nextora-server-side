package lk.iit.nextora.module.club.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.club.dto.request.CreateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.request.UpdateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.response.ClubAnnouncementResponse;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import lk.iit.nextora.module.club.mapper.ClubMapper;
import lk.iit.nextora.module.club.repository.ClubAnnouncementRepository;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.club.service.ClubActivityLogService;
import lk.iit.nextora.config.S3.S3Service;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClubAnnouncementServiceImpl Unit Tests")
class ClubAnnouncementServiceImplTest {

    @Mock private ClubAnnouncementRepository announcementRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private ClubMembershipRepository membershipRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private SecurityService securityService;
    @Mock private ClubMapper clubMapper;
    @Mock private ClubActivityLogService activityLogService;
    @Mock private S3Service s3Service;

    @InjectMocks
    private ClubAnnouncementServiceImpl announcementService;

    // ============================================================
    // CREATE ANNOUNCEMENT TESTS
    // ============================================================

    @Nested
    @DisplayName("createAnnouncement")
    class CreateAnnouncementTests {

        @Test
        @DisplayName("Should create announcement with attachment successfully")
        void createAnnouncement_withAttachment_success() {
            // Given
            Long clubId = 1L;
            Long currentUserId = 1L;
            CreateAnnouncementRequest request = CreateAnnouncementRequest.builder()
                    .clubId(clubId)
                    .title("Meeting Tomorrow")
                    .content("Club meeting at 5 PM")
                    .priority(ClubAnnouncement.AnnouncementPriority.HIGH)
                    .isPinned(true)
                    .isMembersOnly(false)
                    .build();

            MultipartFile attachment = new MockMultipartFile(
                    "file", "document.pdf", "application/pdf", "test content".getBytes()
            );

            Club club = Club.builder()
                    .id(clubId)
                    .clubCode("TECH001")
                    .name("Tech Club")
                    .build();

            Student author = Student.builder()
                    .studentId("IIT/2023/001")
                    .build();

            ClubAnnouncement announcement = ClubAnnouncement.builder()
                    .id(1L)
                    .club(club)
                    .title("Meeting Tomorrow")
                    .content("Club meeting at 5 PM")
                    .author(author)
                    .priority(ClubAnnouncement.AnnouncementPriority.HIGH)
                    .isPinned(true)
                    .attachmentUrl("https://s3.com/clubs/announcements/document.pdf")
                    .attachmentName("document.pdf")
                    .build();

            ClubAnnouncementResponse expectedResponse = ClubAnnouncementResponse.builder()
                    .id(1L)
                    .clubId(clubId)
                    .title("Meeting Tomorrow")
                    .content("Club meeting at 5 PM")
                    .authorName("John Doe")
                    .isPinned(true)
                    .build();

            when(securityService.getCurrentUserId()).thenReturn(currentUserId);
            when(clubRepository.findByIdAndIsDeletedFalse(clubId)).thenReturn(Optional.of(club));
            when(studentRepository.findById(currentUserId)).thenReturn(Optional.of(author));
            when(s3Service.uploadFilePublic(attachment, "clubs/announcements"))
                    .thenReturn("https://s3.com/clubs/announcements/document.pdf");
            when(announcementRepository.save(any(ClubAnnouncement.class))).thenReturn(announcement);
            when(clubMapper.toResponse(announcement)).thenReturn(expectedResponse);

            // When
            ClubAnnouncementResponse response = announcementService.createAnnouncement(request, attachment);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Meeting Tomorrow");
            assertThat(response.getIsPinned()).isTrue();
            verify(announcementRepository, times(1)).save(any(ClubAnnouncement.class));
            verify(s3Service, times(1)).uploadFilePublic(attachment, "clubs/announcements");
        }

        @Test
        @DisplayName("Should create announcement without attachment successfully")
        void createAnnouncement_withoutAttachment_success() {
            // Given
            Long clubId = 1L;
            Long currentUserId = 1L;
            CreateAnnouncementRequest request = CreateAnnouncementRequest.builder()
                    .clubId(clubId)
                    .title("New Event")
                    .content("Event details")
                    .priority(ClubAnnouncement.AnnouncementPriority.NORMAL)
                    .isPinned(false)
                    .isMembersOnly(true)
                    .build();

            Club club = Club.builder().id(clubId).clubCode("TECH001").build();
            Student author = Student.builder().studentId("IIT/2023/001").build();

            ClubAnnouncement announcement = ClubAnnouncement.builder()
                    .id(2L)
                    .club(club)
                    .title("New Event")
                    .content("Event details")
                    .author(author)
                    .isMembersOnly(true)
                    .build();

            ClubAnnouncementResponse expectedResponse = ClubAnnouncementResponse.builder()
                    .id(2L)
                    .clubId(clubId)
                    .title("New Event")
                    .authorName("Jane Doe")
                    .isMembersOnly(true)
                    .build();

            when(securityService.getCurrentUserId()).thenReturn(currentUserId);
            when(clubRepository.findByIdAndIsDeletedFalse(clubId)).thenReturn(Optional.of(club));
            when(studentRepository.findById(currentUserId)).thenReturn(Optional.of(author));
            when(announcementRepository.save(any(ClubAnnouncement.class))).thenReturn(announcement);
            when(clubMapper.toResponse(announcement)).thenReturn(expectedResponse);

            // When
            ClubAnnouncementResponse response = announcementService.createAnnouncement(request, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("New Event");
            verify(s3Service, never()).uploadFilePublic(any(), any());
        }
    }

    // ============================================================
    // UPDATE ANNOUNCEMENT TESTS
    // ============================================================

    @Nested
    @DisplayName("updateAnnouncement")
    class UpdateAnnouncementTests {

        @Test
        @DisplayName("Should update announcement successfully")
        void updateAnnouncement_success() {
            // Given
            Long announcementId = 1L;
            UpdateAnnouncementRequest request = UpdateAnnouncementRequest.builder()
                    .title("Updated Title")
                    .content("Updated content")
                    .isPinned(true)
                    .build();

            Club club = Club.builder().id(1L).clubCode("TECH001").build();
            ClubAnnouncement existingAnnouncement = ClubAnnouncement.builder()
                    .id(announcementId)
                    .club(club)
                    .title("Old Title")
                    .content("Old content")
                    .isPinned(false)
                    .build();

            ClubAnnouncement updatedAnnouncement = ClubAnnouncement.builder()
                    .id(announcementId)
                    .club(club)
                    .title("Updated Title")
                    .content("Updated content")
                    .isPinned(true)
                    .build();

            ClubAnnouncementResponse expectedResponse = ClubAnnouncementResponse.builder()
                    .id(announcementId)
                    .clubId(1L)
                    .title("Updated Title")
                    .isPinned(true)
                    .build();

            when(announcementRepository.findByIdAndIsDeletedFalse(announcementId))
                    .thenReturn(Optional.of(existingAnnouncement));
            when(announcementRepository.save(any(ClubAnnouncement.class))).thenReturn(updatedAnnouncement);
            when(clubMapper.toResponse(updatedAnnouncement)).thenReturn(expectedResponse);

            // When
            ClubAnnouncementResponse response = announcementService.updateAnnouncement(announcementId, request, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Updated Title");
            assertThat(response.getIsPinned()).isTrue();
            verify(announcementRepository, times(1)).save(any(ClubAnnouncement.class));
        }
    }

    // ============================================================
    // DELETE ANNOUNCEMENT TESTS
    // ============================================================

    @Nested
    @DisplayName("deleteAnnouncement")
    class DeleteAnnouncementTests {

        @Test
        @DisplayName("Should delete announcement successfully")
        void deleteAnnouncement_success() {
            // Given
            Long announcementId = 1L;

            Club club = Club.builder().id(1L).clubCode("TECH001").build();
            ClubAnnouncement announcement = ClubAnnouncement.builder()
                    .id(announcementId)
                    .club(club)
                    .title("To Delete")
                    .isDeleted(false)
                    .build();

            when(announcementRepository.findByIdAndIsDeletedFalse(announcementId))
                    .thenReturn(Optional.of(announcement));
            when(announcementRepository.save(any(ClubAnnouncement.class))).thenReturn(announcement);

            // When
            announcementService.deleteAnnouncement(announcementId);

            // Then
            verify(announcementRepository, times(1)).save(any(ClubAnnouncement.class));
        }

        @Test
        @DisplayName("Should throw exception when announcement not found")
        void deleteAnnouncement_notFound_throwsException() {
            // Given
            Long announcementId = 999L;
            when(announcementRepository.findByIdAndIsDeletedFalse(announcementId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> announcementService.deleteAnnouncement(announcementId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

