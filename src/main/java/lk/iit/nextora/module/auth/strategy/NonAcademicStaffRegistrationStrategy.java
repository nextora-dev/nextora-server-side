package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.module.auth.dto.request.NonAcademicStaffRegisterRequest;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.NonAcademicStaff;
import lk.iit.nextora.module.auth.mapper.UserMapper;
import lk.iit.nextora.module.auth.repository.NonAcademicStaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonAcademicStaffRegistrationStrategy implements RegistrationStrategy {

    private final NonAcademicStaffRepository nonAcademicStaffRepository;
    private final UserMapper userMapper;

    @Override
    public void validate(RegisterRequest request) {
        if (!(request instanceof NonAcademicStaffRegisterRequest)) {
            throw new BadRequestException("Invalid request type for Non-Academic Staff registration");
        }

        NonAcademicStaffRegisterRequest staffRequest = (NonAcademicStaffRegisterRequest) request;

        // Check if employee ID already exists
        if (nonAcademicStaffRepository.existsByEmployeeId(staffRequest.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        // Optional: validate join date
        if (staffRequest.getJoinDate() != null &&
                staffRequest.getJoinDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Join date cannot be in the future");
        }
    }

    @Override
    public BaseUser mapToEntity(RegisterRequest request) {
        NonAcademicStaffRegisterRequest staffRequest = (NonAcademicStaffRegisterRequest) request;

        // Use mapper to convert request to entity
        NonAcademicStaff staff = userMapper.toNonAcademicStaff(staffRequest);

        // Set common fields from base request
        staff.setEmail(staffRequest.getEmail());
        staff.setFirstName(staffRequest.getFirstName());
        staff.setLastName(staffRequest.getLastName());

        return staff;
    }

    @Override
    public void postRegistration(BaseUser user) {
        NonAcademicStaff staff = (NonAcademicStaff) user;
        log.info("Non-Academic Staff registered successfully: {} - {}",
                staff.getEmployeeId(), staff.getEmail());
    }
}
