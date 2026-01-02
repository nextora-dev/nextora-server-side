package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.dto.request.StudentRegisterRequest;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentRegistrationStrategy implements RegistrationStrategy {

    private final StudentRepository studentRepository;

    @Override
    public void validate(RegisterRequest request) {
        if (!(request instanceof StudentRegisterRequest)) {
            throw new BadRequestException("Invalid request type for student registration");
        }

        StudentRegisterRequest studentRequest = (StudentRegisterRequest) request;

        // Check if student ID already exists
        if (studentRepository.existsByStudentId(studentRequest.getStudentId())) {
            throw new BadRequestException("Student ID already exists");
        }

        // Validate date of birth
        if (studentRequest.getDateOfBirth() != null) {
            if (studentRequest.getDateOfBirth().isAfter(LocalDate.now().minusYears(16))) {
                throw new BadRequestException("Student must be at least 16 years old");
            }
        }
    }

    @Override
    public BaseUser mapToEntity(RegisterRequest request) {
        StudentRegisterRequest studentRequest = (StudentRegisterRequest) request;

        Student student = Student.builder()
                .studentId(studentRequest.getStudentId())
                .batch(studentRequest.getBatch())
                .program(studentRequest.getProgram())
                .faculty(studentRequest.getFaculty())
                .dateOfBirth(studentRequest.getDateOfBirth())
                .address(studentRequest.getAddress())
                .guardianName(studentRequest.getGuardianName())
                .guardianPhone(studentRequest.getGuardianPhone())
                .build();

        student.setEmail(studentRequest.getEmail());
        student.setFirstName(studentRequest.getFirstName());
        student.setLastName(studentRequest.getLastName());
        student.setPhoneNumber(studentRequest.getPhone());

        return student;
    }

    @Override
    public void postRegistration(BaseUser user) {
        Student student = (Student) user;
        log.info("Student registered successfully: {} - {}",
                student.getStudentId(), student.getEmail());
    }
}