package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.module.auth.dto.request.AdminRegisterRequest;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.entity.Admin;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.mapper.UserMapper;
import lk.iit.nextora.module.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminRegistrationStrategy implements RegistrationStrategy {

    private final AdminRepository adminRepository;
    private final UserMapper userMapper;

    @Override
    public void validate(RegisterRequest request) {
        if (!(request instanceof AdminRegisterRequest)) {
            throw new BadRequestException("Invalid request type for Admin registration");
        }

        AdminRegisterRequest adminRequest = (AdminRegisterRequest) request;

        // Check if admin ID already exists
        if (adminRepository.existsByAdminId(adminRequest.getAdminId())) {
            throw new BadRequestException("Admin ID already exists");
        }

        // Optional: Validate assigned date
        if (adminRequest.getAssignedDate() != null &&
                adminRequest.getAssignedDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Assigned date cannot be in the future");
        }
    }

    @Override
    public BaseUser mapToEntity(RegisterRequest request) {
        AdminRegisterRequest adminRequest = (AdminRegisterRequest) request;

        // Use mapper to convert request to entity
        Admin admin = userMapper.toAdmin(adminRequest);

        // Set common fields from base request
        admin.setEmail(adminRequest.getEmail());
        admin.setFirstName(adminRequest.getFirstName());
        admin.setLastName(adminRequest.getLastName());

        return admin;
    }

    @Override
    public void postRegistration(BaseUser user) {
        Admin admin = (Admin) user;
        log.info("Admin registered successfully: {} - {}",
                admin.getAdminId(), admin.getEmail());
    }
}
