package lk.iit.nextora.module.auth.service;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.request.FirstTimePasswordChangeRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
public interface AdminUserManagementService {
    UserCreatedResponse createUser(AdminCreateUserRequest request);
    AuthResponse changePasswordOnFirstLogin(FirstTimePasswordChangeRequest request);
}
