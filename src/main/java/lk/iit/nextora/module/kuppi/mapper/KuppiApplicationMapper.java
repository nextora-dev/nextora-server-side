package lk.iit.nextora.module.kuppi.mapper;

import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.kuppi.dto.request.KuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiApplication;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Mapper for Kuppi Application entities and DTOs
 */
@Component
public class KuppiApplicationMapper {

    /**
     * Map request DTO to entity
     */
    public KuppiApplication toEntity(KuppiApplicationRequest request, Student student) {
        return KuppiApplication.builder()
                .student(student)
                .motivation(request.getMotivation())
                .relevantExperience(request.getRelevantExperience())
                .subjectsToTeach(new HashSet<>(request.getSubjectsToTeach()))
                .preferredExperienceLevel(request.getPreferredExperienceLevel())
                .availability(request.getAvailability())
                .currentGpa(request.getCurrentGpa())
                .currentSemester(request.getCurrentSemester())
                .build();
    }

    /**
     * Map entity to response DTO
     */
    public KuppiApplicationResponse toResponse(KuppiApplication application) {
        KuppiApplicationResponse.KuppiApplicationResponseBuilder builder = KuppiApplicationResponse.builder()
                .id(application.getId())
                .status(application.getStatus())
                .statusDisplayName(application.getStatus().getDisplayName())
                .motivation(application.getMotivation())
                .relevantExperience(application.getRelevantExperience())
                .subjectsToTeach(application.getSubjectsToTeach())
                .preferredExperienceLevel(application.getPreferredExperienceLevel())
                .availability(application.getAvailability())
                .currentGpa(application.getCurrentGpa())
                .currentSemester(application.getCurrentSemester())
                .academicResultsUrl(application.getAcademicResultsUrl())
                .academicResultsFileName(application.getAcademicResultsFileName())
                .reviewNotes(application.getReviewNotes())
                .rejectionReason(application.getRejectionReason())
                .submittedAt(application.getSubmittedAt())
                .approvedAt(application.getApprovedAt())
                .rejectedAt(application.getRejectedAt())
                .cancelledAt(application.getCancelledAt())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .canBeApproved(application.canBeApproved())
                .canBeRejected(application.canBeRejected())
                .canBeCancelled(application.canBeCancelled())
                .isFinalState(application.isFinalState());

        // Map student info
        if (application.getStudent() != null) {
            Student student = application.getStudent();
            builder.studentId(student.getId())
                    .studentUserId(student.getStudentId())
                    .studentName(student.getFullName())
                    .studentEmail(student.getEmail())
                    .studentBatch(student.getBatch())
                    .studentProgram(student.getProgram())
                    .studentFaculty(student.getFaculty() != null ? student.getFaculty().name() : null)
                    .studentProfilePictureUrl(student.getProfilePictureUrl());
        }

        // Map reviewer info
        if (application.getReviewedBy() != null) {
            builder.reviewedById(application.getReviewedBy().getId())
                    .reviewedByName(application.getReviewedBy().getFullName())
                    .reviewedByEmail(application.getReviewedBy().getEmail())
                    .reviewedAt(application.getReviewedAt());
        }

        return builder.build();
    }
}

