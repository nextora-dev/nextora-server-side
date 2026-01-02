package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.module.auth.dto.request.LecturerRegisterRequest;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Lecturer;
import lk.iit.nextora.module.auth.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LecturerRegistrationStrategy implements RegistrationStrategy {

    private final LecturerRepository lecturerRepository;

    @Override
    public void validate(RegisterRequest request) {
        if (!(request instanceof LecturerRegisterRequest)) {
            throw new BadRequestException("Invalid request type for lecturer registration");
        }

        LecturerRegisterRequest lecturerRequest = (LecturerRegisterRequest) request;

        if (lecturerRepository.existsByEmployeeId(lecturerRequest.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }
    }

    @Override
    public BaseUser mapToEntity(RegisterRequest request) {
        LecturerRegisterRequest lecturerRequest = (LecturerRegisterRequest) request;

        Lecturer lecturer = Lecturer.builder()
                .employeeId(lecturerRequest.getEmployeeId())
                .department(lecturerRequest.getDepartment())
                .faculty(lecturerRequest.getFaculty())
                .designation(lecturerRequest.getDesignation())
                .specialization(lecturerRequest.getSpecialization())
                .qualifications(lecturerRequest.getQualifications())
                .officeLocation(lecturerRequest.getOfficeLocation())
                .bio(lecturerRequest.getBio())
                .build();

        lecturer.setEmail(lecturerRequest.getEmail());
        lecturer.setFirstName(lecturerRequest.getFirstName());
        lecturer.setLastName(lecturerRequest.getLastName());
        lecturer.setPhoneNumber(lecturerRequest.getPhone());

        return lecturer;
    }

    @Override
    public void postRegistration(BaseUser user) {
        Lecturer lecturer = (Lecturer) user;
        log.info("Lecturer registered successfully: {} - {}",
                lecturer.getEmployeeId(), lecturer.getEmail());
    }
}