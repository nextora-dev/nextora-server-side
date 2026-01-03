package lk.iit.nextora.module.auth.mapper;

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
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface UserMapper {

    // ==================== Student Mappings ====================

    /**
     * Map StudentRegisterRequest to Student entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "enrollmentDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Student toStudent(StudentRegisterRequest request);

    /**
     * Update existing Student entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    void updateStudentFromRequest(StudentRegisterRequest request, @MappingTarget Student student);

    // ==================== Lecturer Mappings ====================

    /**
     * Map LecturerRegisterRequest to Lecturer entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "joinDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Lecturer toLecturer(LecturerRegisterRequest request);

    /**
     * Update existing Lecturer entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    void updateLecturerFromRequest(LecturerRegisterRequest request, @MappingTarget Lecturer lecturer);

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

