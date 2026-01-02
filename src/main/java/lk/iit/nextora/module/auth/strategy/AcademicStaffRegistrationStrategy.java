package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.module.auth.dto.request.AcademicStaffRegisterRequest;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.BaseUser;
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

        AcademicStaff staff = AcademicStaff.builder()
                .employeeId(staffRequest.getEmployeeId())
                .department(staffRequest.getDepartment())
                .position(staffRequest.getPosition())
                .officeLocation(staffRequest.getOfficeLocation())
                .joinDate(staffRequest.getJoinDate())
                .responsibilities(staffRequest.getResponsibilities())
                .build();

        staff.setEmail(staffRequest.getEmail());
        staff.setFirstName(staffRequest.getFirstName());
        staff.setLastName(staffRequest.getLastName());
        staff.setPhoneNumber(staffRequest.getPhone());

        return staff;
    }

    @Override
    public void postRegistration(BaseUser user) {
        AcademicStaff staff = (AcademicStaff) user;
        log.info("Academic Staff registered successfully: {} - {}",
                staff.getEmployeeId(), staff.getEmail());
    }
}
