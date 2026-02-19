package lk.iit.nextora.module.user.service;

import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture);

    void changePassword(ChangePasswordRequest request);
}

