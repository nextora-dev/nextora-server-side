package lk.iit.nextora.module.user.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User profile conversions
 */
@Mapper(config = MapperConfiguration.class,
        uses = {UserResponseMapper.class})
public interface UserProfileMapper {

    /**
     * Map BaseUser entity to UserProfileResponse
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roleSpecificData", ignore = true)
    UserProfileResponse toProfileResponse(BaseUser user);

    /**
     * Map BaseUser entity to UserSummaryResponse
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "active", expression = "java(user.isActive())")
    @Mapping(target = "status", source = "status")
    UserSummaryResponse toSummaryResponse(BaseUser user);

    /**
     * Build full profile response with role-specific data
     */
    default UserProfileResponse toFullProfileResponse(BaseUser user, UserResponseMapper userResponseMapper) {
        UserProfileResponse response = toProfileResponse(user);
        response.setRoleSpecificData(userResponseMapper.extractRoleSpecificData(user));
        return response;
    }
}

