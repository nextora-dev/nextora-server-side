package lk.iit.nextora.module.auth.dto.response;

import lk.iit.nextora.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    // Role-specific data (can be extended)
    private Object roleSpecificData;
}