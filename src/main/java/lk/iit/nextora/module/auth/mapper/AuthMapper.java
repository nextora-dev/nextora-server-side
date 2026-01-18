package lk.iit.nextora.module.auth.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.entity.BaseUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Date;

/**
 * MapStruct mapper for authentication-related conversions
 */
@Mapper(config = MapperConfiguration.class)
public interface AuthMapper {

    /**
     * Convert BaseUser entity to AuthResponse DTO
     *
     * @param user         the user entity
     * @param accessToken  JWT access token
     * @param refreshToken JWT refresh token
     * @param expiresIn    token expiration date
     * @return AuthResponse DTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "userType", source = "user.userType")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "expiresIn", source = "expiresIn")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "roleSpecificData", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    AuthResponse toAuthResponse(BaseUser user, String accessToken, String refreshToken, Date expiresIn);

    /**
     * Build AuthResponse with role-specific data
     *
     * @param user             the user entity
     * @param accessToken      JWT access token
     * @param refreshToken     JWT refresh token
     * @param expiresIn        token expiration date
     * @param roleSpecificData additional role-specific data
     * @return AuthResponse DTO with role-specific data
     */
    default AuthResponse toAuthResponseWithRoleData(
            BaseUser user,
            String accessToken,
            String refreshToken,
            Date expiresIn,
            Object roleSpecificData) {

        AuthResponse response = toAuthResponse(user, accessToken, refreshToken, expiresIn);
        response.setRoleSpecificData(roleSpecificData);
        return response;
    }

    /**
     * Build AuthResponse for pending verification (no tokens)
     *
     * @param user    the user entity
     * @param message message for the user
     * @return AuthResponse DTO without tokens
     */
    default AuthResponse toPendingVerificationResponse(BaseUser user, String message) {
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setUserType(user.getUserType());
        response.setAccessToken(null);
        response.setRefreshToken(null);
        response.setExpiresIn(null);
        response.setTokenType(null);
        response.setMessage(message);
        response.setEmailVerified(false);
        return response;
    }
}

