package lk.iit.nextora.module.auth.mapper;

import lk.iit.nextora.common.mapper.DateTimeMapper;
import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.auth.dto.request.*;
import lk.iit.nextora.module.auth.entity.*;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity conversions
 * Maps between registration request DTOs and User entities
 *
 * Uses default constructor + setters instead of builder pattern
 * because Lombok @Builder doesn't include inherited fields from BaseUser
 */
@Mapper(config = MapperConfiguration.class,
        uses = {DateTimeMapper.class},
        builder = @Builder(disableBuilder = true))
public interface UserMapper {

    // ==================== Student Mappings ====================

    /**
     * Map StudentRegisterRequest to Student entity.
     * Maps all fields including role-specific fields.
     * Now supports multiple student role types.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "enrollmentDate", ignore = true)
    @Mapping(target = "studentRoleTypes", expression = "java(mapStudentRoleTypes(request))")
    @Mapping(target = "kuppiSessionsCompleted", constant = "0")
    @Mapping(target = "kuppiRating", constant = "0.0")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Student toStudent(StudentRegisterRequest request);

    /**
     * Map student role types from request - handles both new and deprecated fields
     */
    default java.util.Set<lk.iit.nextora.common.enums.StudentRoleType> mapStudentRoleTypes(StudentRegisterRequest request) {
        java.util.Set<lk.iit.nextora.common.enums.StudentRoleType> roleTypes = java.util.EnumSet.of(lk.iit.nextora.common.enums.StudentRoleType.NORMAL);

        // First check new field (studentRoleTypes)
        if (request.getStudentRoleTypes() != null && !request.getStudentRoleTypes().isEmpty()) {
            roleTypes.addAll(request.getStudentRoleTypes());
        }
        // Fallback to deprecated field for backward compatibility
        else if (request.getStudentRoleType() != null && request.getStudentRoleType() != lk.iit.nextora.common.enums.StudentRoleType.NORMAL) {
            roleTypes.add(request.getStudentRoleType());
        }

        return roleTypes;
    }

    /**
     * Update existing Student entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "studentRoleTypes", ignore = true)
    @Mapping(target = "kuppiSessionsCompleted", ignore = true)
    @Mapping(target = "kuppiRating", ignore = true)
    void updateStudentFromRequest(StudentRegisterRequest request, @MappingTarget Student student);


    // ==================== Admin Mappings ====================

    /**
     * Map AdminRegisterRequest to Admin entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Admin toAdmin(AdminRegisterRequest request);

    /**
     * Update existing Admin entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    void updateAdminFromRequest(AdminRegisterRequest request, @MappingTarget Admin admin);

    // ==================== Academic Staff Mappings ====================

    /**
     * Map AcademicStaffRegisterRequest to AcademicStaff entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AcademicStaff toAcademicStaff(AcademicStaffRegisterRequest request);

    /**
     * Update existing AcademicStaff entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    void updateAcademicStaffFromRequest(AcademicStaffRegisterRequest request, @MappingTarget AcademicStaff staff);

    // ==================== Non-Academic Staff Mappings ====================

    /**
     * Map NonAcademicStaffRegisterRequest to NonAcademicStaff entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "workLocation", source = "officeLocation")
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NonAcademicStaff toNonAcademicStaff(NonAcademicStaffRegisterRequest request);

    /**
     * Update existing NonAcademicStaff entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "workLocation", source = "officeLocation")
    void updateNonAcademicStaffFromRequest(NonAcademicStaffRegisterRequest request, @MappingTarget NonAcademicStaff staff);

    // ==================== Super Admin Mappings ====================

    /**
     * Map SuperAdminRegisterRequest to SuperAdmin entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SuperAdmin toSuperAdmin(SuperAdminRegisterRequest request);

    /**
     * Update existing SuperAdmin entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    void updateSuperAdminFromRequest(SuperAdminRegisterRequest request, @MappingTarget SuperAdmin superAdmin);
}

