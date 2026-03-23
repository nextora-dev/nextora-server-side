package lk.iit.nextora.module.user.dto.response;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private UserRole role;
    private UserStatus status;
    private String userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Role-specific data (studentId, employeeId, department, etc.)
    private Object roleSpecificData;
}

