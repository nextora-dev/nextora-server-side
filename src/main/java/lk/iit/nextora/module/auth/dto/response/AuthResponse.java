package lk.iit.nextora.module.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.iit.nextora.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Authentication response DTO used for both login and registration responses.
 * Contains JWT tokens and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Date expiresIn;

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String userType;

    private Object roleSpecificData;

    // Status fields
    private String message;
    private Boolean passwordChangeRequired;
}
