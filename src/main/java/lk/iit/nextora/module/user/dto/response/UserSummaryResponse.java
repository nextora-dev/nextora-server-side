package lk.iit.nextora.module.user.dto.response;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for user lists
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private UserRole role;
    private String userType;
    private Boolean active;
    private UserStatus status;
}

