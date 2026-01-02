package lk.iit.nextora.module.auth.factory;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.module.auth.strategy.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationStrategyFactory {

    private final StudentRegistrationStrategy studentStrategy;
    private final LecturerRegistrationStrategy lecturerStrategy;
    private final AcademicStaffRegistrationStrategy academicStaffStrategy;
    private final NonAcademicStaffRegistrationStrategy nonAcademicStaffStrategy;
    private final AdminRegistrationStrategy adminStrategy;
    private final SuperAdminRegistrationStrategy superAdminStrategy;

    public RegistrationStrategy getStrategy(UserRole role) {
        log.debug("Getting registration strategy for role: {}", role);

        return switch (role) {
            case ROLE_STUDENT -> studentStrategy;
            case ROLE_LECTURER -> lecturerStrategy;
            case ROLE_ACADEMIC_STAFF -> academicStaffStrategy;
            case ROLE_NON_ACADEMIC_STAFF -> nonAcademicStaffStrategy;
            case ROLE_ADMIN -> adminStrategy;
            case ROLE_SUPER_ADMIN -> superAdminStrategy;
        };
    }
}