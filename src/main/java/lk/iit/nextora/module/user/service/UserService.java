package lk.iit.nextora.module.user.service;

import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;

import java.util.List;

public interface UserService {

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    UserProfileResponse getUserById(Long id);

    List<UserSummaryResponse> getAllUsers();

    UserProfileResponse createAdminUser(CreateAdminRequest request);

    UserProfileResponse updateUserById(Long id, UpdateProfileRequest request);

    void deleteUser(Long id);

    void restoreUser(Long id);

    void activateUser(Long id);

    void deactivateUser(Long id);

    void resetUserPassword(Long id);

    void unlockUser(Long id);

    UserCreatedResponse createUser(AdminCreateUserRequest request);
}

