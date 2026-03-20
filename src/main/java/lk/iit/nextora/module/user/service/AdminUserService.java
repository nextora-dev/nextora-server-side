package lk.iit.nextora.module.user.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminUserService {

    UserStatsSummaryResponse getUserStatsSummary();

    UserProfileResponse getNormalUserById(Long id);

    PagedResponse<UserSummaryResponse> getAllNormalUsers(Pageable pageable);

    PagedResponse<UserSummaryResponse> searchNormalUsers(String keyword, Pageable pageable);

    PagedResponse<UserSummaryResponse> filterNormalUsers(List<UserRole> roles, List<UserStatus> statuses, Pageable pageable);

    void deleteNormalUser(Long id);

    UserCreatedResponse createNormalUser(AdminCreateUserRequest request);

    UserProfileResponse updateNormalUserById(Long id, UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture);

    void activateNormalUser(Long id);

    void deactivateNormalUser(Long id);

    void suspendNormalUser(Long id, String reason);

    void resetNormalUserPassword(Long id);

    void unlockNormalUser(Long id);
}

