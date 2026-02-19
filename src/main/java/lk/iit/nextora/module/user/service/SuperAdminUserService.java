package lk.iit.nextora.module.user.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateAdminRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface SuperAdminUserService {

    void restoreUser(Long id);

    UserProfileResponse createAdminUser(CreateAdminRequest request);

    void permanentlyDeleteUser(Long id);

    PagedResponse<UserSummaryResponse> getAllAdminUsers(Pageable pageable);

    UserProfileResponse getAdminUserById(Long adminId);

    UserProfileResponse updateAdminUser(Long adminId, UpdateAdminRequest request);

    void softDeleteAdminUser(Long adminId);
}

