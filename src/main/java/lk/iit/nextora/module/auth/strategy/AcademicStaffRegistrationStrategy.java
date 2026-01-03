package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.module.auth.dto.request.AcademicStaffRegisterRequest;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.mapper.UserMapper;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicStaffRegistrationStrategy implements RegistrationStrategy {

    private final AcademicStaffRepository academicStaffRepository;
    private final UserMapper userMapper;

    @Override
    public void validate(RegisterRequest request) {
        if (!(request instanceof AcademicStaffRegisterRequest)) {
            throw new BadRequestException("Invalid request type for Academic Staff registration");
        }

        AcademicStaffRegisterRequest staffRequest = (AcademicStaffRegisterRequest) request;

        // Check if employee ID already exists
        if (academicStaffRepository.existsByEmployeeId(staffRequest.getEmployeeId())) {
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
        AcademicStaffRegisterRequest staffRequest = (AcademicStaffRegisterRequest) request;

        // Use mapper to convert request to entity
        AcademicStaff staff = userMapper.toAcademicStaff(staffRequest);

        // Set common fields from base request
        staff.setEmail(staffRequest.getEmail());
        staff.setFirstName(staffRequest.getFirstName());
        staff.setLastName(staffRequest.getLastName());

        return staff;
    }

    @Override
    public void postRegistration(BaseUser user) {
        AcademicStaff staff = (AcademicStaff) user;
        log.info("Academic Staff registered successfully: {} - {}",
                staff.getEmployeeId(), staff.getEmail());
    }
}
