package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.dto.request.SuperAdminRegisterRequest;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.SuperAdmin;
import lk.iit.nextora.module.auth.mapper.UserMapper;
import lk.iit.nextora.module.auth.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminRegistrationStrategy implements RegistrationStrategy {

    private final SuperAdminRepository superAdminRepository;
    private final UserMapper userMapper;

    @Override
    public void validate(RegisterRequest request) {

        if (!(request instanceof SuperAdminRegisterRequest)) {
            throw new BadRequestException(
                    "Invalid request type for Super Admin registration"
            );
        }

        SuperAdminRegisterRequest superAdminRequest =
                (SuperAdminRegisterRequest) request;

        // Check if Super Admin ID already exists
        if (superAdminRepository.existsBySuperAdminId(
                superAdminRequest.getSuperAdminId())) {
            throw new BadRequestException("Super Admin ID already exists");
        }

        // Optional: validate assigned date
        if (superAdminRequest.getAssignedDate() != null &&
                superAdminRequest.getAssignedDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Assigned date cannot be in the future");
        }
    }

    @Override
    public BaseUser mapToEntity(RegisterRequest request) {

        SuperAdminRegisterRequest superAdminRequest =
                (SuperAdminRegisterRequest) request;

        // Use mapper to convert request to entity
        SuperAdmin superAdmin = userMapper.toSuperAdmin(superAdminRequest);

        // Set common fields from base request
        superAdmin.setEmail(superAdminRequest.getEmail());
        superAdmin.setFirstName(superAdminRequest.getFirstName());
        superAdmin.setLastName(superAdminRequest.getLastName());

        return superAdmin;
    }

    @Override
    public void postRegistration(BaseUser user) {
        SuperAdmin superAdmin = (SuperAdmin) user;
        log.info(
                "Super Admin registered successfully: {} - {}",
                superAdmin.getSuperAdminId(),
                superAdmin.getEmail()
        );
    }
}
