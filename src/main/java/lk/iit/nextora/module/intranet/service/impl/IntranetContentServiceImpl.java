package lk.iit.nextora.module.intranet.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.util.JsonUtils;
import lk.iit.nextora.module.intranet.dto.*;
import lk.iit.nextora.module.intranet.entity.*;
import lk.iit.nextora.module.intranet.mapper.IntranetMapper;
import lk.iit.nextora.module.intranet.repository.*;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Database-backed service implementation for intranet content.
 * Uses JPA repositories for persistence, MapStruct for entity→response mapping,
 * and JsonUtils for polymorphic nested JSON data.
 *
 * @author nextora-team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("Convert2Diamond") // TypeReference<T> requires explicit type args for runtime type capture
public class IntranetContentServiceImpl implements IntranetContentService {

    private final StudentComplaintCategoryRepository complaintRepo;
    private final AcademicCalendarRepository calendarRepo;
    private final ProgramRepository programRepo;
    private final FoundationCategoryRepository foundationRepo;
    private final SruCategoryRepository sruRepo;
    private final StudentPolicyRepository policyRepo;
    private final MitigationFormRepository mitigationRepo;
    private final StaffCategoryRepository staffRepo;
    private final InfoCategoryRepository infoRepo;
    private final ScheduleCategoryRepository scheduleRepo;
    private final IntranetMapper mapper;

    private static final String UNDERGRADUATE = "UNDERGRADUATE";
    private static final String POSTGRADUATE = "POSTGRADUATE";

    // ==================== Schedules ====================

    @Override
    public List<ScheduleCategoryResponse> getAllScheduleCategories() {
        log.debug("Querying all active schedule categories");
        List<ScheduleCategory> entities = scheduleRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();
        log.info("Retrieved {} schedule categories", entities.size());
        return mapper.toScheduleCategorySummaryResponseList(entities);
    }

    @Override
    public ScheduleCategoryResponse getScheduleCategoryBySlug(String slug) {
        log.debug("Querying schedule category by slug: {}", slug);
        ScheduleCategory entity = scheduleRepo.findByCategorySlugWithEvents(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule category", "slug", slug));
        return mapper.toScheduleCategoryResponse(entity);
    }

    // ==================== Student Complaints ====================

    @Override
    public List<StudentComplaintCategoryResponse> getAllStudentComplaintCategories() {
        log.debug("Querying all active student complaint categories");
        List<StudentComplaintCategory> entities = complaintRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();
        log.info("Retrieved {} student complaint categories", entities.size());
        return mapper.toComplaintSummaryResponseList(entities);
    }

    @Override
    public StudentComplaintCategoryResponse getStudentComplaintBySlug(String slug) {
        log.debug("Querying student complaint category by slug: {}", slug);
        StudentComplaintCategory entity = complaintRepo.findByCategorySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Student complaint category", "slug", slug));
        return mapper.toComplaintResponse(entity);
    }

    // ==================== Academic Calendars ====================

    @Override
    public List<AcademicCalendarResponse> getAllAcademicCalendars() {
        log.debug("Querying all active academic calendars");
        List<AcademicCalendar> entities = calendarRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByUniversityNameAsc();
        log.info("Retrieved {} academic calendars", entities.size());
        return mapper.toCalendarSummaryResponseList(entities);
    }

    @Override
    public AcademicCalendarResponse getAcademicCalendarBySlug(String slug) {
        log.debug("Querying academic calendar by slug: {}", slug);
        AcademicCalendar entity = calendarRepo.findByUniversitySlugWithEvents(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Academic calendar", "slug", slug));
        return mapper.toCalendarResponse(entity);
    }

    // ==================== Programs (UG / PG) ====================

    @Override
    public List<ProgramResponse> getAllUndergraduatePrograms() {
        return getAllProgramsByLevel(UNDERGRADUATE);
    }

    @Override
    public ProgramResponse getUndergraduateProgramBySlug(String slug) {
        return getProgramBySlugAndLevel(slug, UNDERGRADUATE);
    }

    @Override
    public List<ProgramResponse> getAllPostgraduatePrograms() {
        return getAllProgramsByLevel(POSTGRADUATE);
    }

    @Override
    public ProgramResponse getPostgraduateProgramBySlug(String slug) {
        return getProgramBySlugAndLevel(slug, POSTGRADUATE);
    }

    private List<ProgramResponse> getAllProgramsByLevel(String level) {
        log.debug("Querying all active {} programs", level.toLowerCase());
        List<Program> entities = programRepo.findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc(level);
        log.info("Retrieved {} {} programs", entities.size(), level.toLowerCase());
        return mapper.toProgramSummaryResponseList(entities);
    }

    private ProgramResponse getProgramBySlugAndLevel(String slug, String level) {
        log.debug("Querying {} program by slug: {}", level.toLowerCase(), slug);
        Program entity = programRepo.findByProgramSlugAndProgramLevelWithModules(slug, level)
                .orElseThrow(() -> new ResourceNotFoundException(level.charAt(0) + level.substring(1).toLowerCase() + " program", "slug", slug));
        if (entity.getCareerProspects() != null) {
            entity.getCareerProspects().size();   // force-load within transaction
        }
        return mapper.toProgramResponse(entity);
    }

    // ==================== Foundation Program ====================

    @Override
    public List<FoundationProgramResponse> getAllFoundationProgramCategories() {
        log.debug("Querying all active foundation program categories");
        List<FoundationCategory> entities = foundationRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();
        log.info("Retrieved {} foundation program categories", entities.size());
        return entities.stream()
                .map(e -> FoundationProgramResponse.builder()
                        .id(e.getId())
                        .categoryName(e.getCategoryName())
                        .categorySlug(e.getCategorySlug())
                        .description(e.getDescription())
                        .build())
                .toList();
    }

    @Override
    public FoundationProgramResponse getFoundationProgramBySlug(String slug) {
        log.debug("Querying foundation program category by slug: {}", slug);
        FoundationCategory entity = foundationRepo.findByCategorySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Foundation program category", "slug", slug));
        return mapFoundationEntity(entity);
    }

    // ==================== Students Relations Unit ====================

    @Override
    public StudentsRelationsUnitResponse getStudentsRelationsUnitInfo() {
        log.debug("Querying Students Relations Unit root info");
        SruCategory root = sruRepo.findRootCategory()
                .orElseThrow(() -> new ResourceNotFoundException("Students Relations Unit", "slug", "_root"));
        return mapSruRoot(root);
    }

    @Override
    public StudentsRelationsUnitResponse getStudentsRelationsUnitBySlug(String slug) {
        log.debug("Querying SRU category by slug: {}", slug);
        SruCategory entity = sruRepo.findByCategorySlugWithVideos(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Students Relations Unit category", "slug", slug));
        return mapSruCategory(entity);
    }

    // ==================== Student Policies ====================

    @Override
    public List<StudentPolicyResponse> getAllStudentPolicies() {
        log.debug("Querying all active student policies");
        List<StudentPolicy> entities = policyRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByPolicyNameAsc();
        log.info("Retrieved {} student policies", entities.size());
        return mapper.toPolicySummaryResponseList(entities);
    }

    @Override
    public StudentPolicyResponse getStudentPolicyBySlug(String slug) {
        log.debug("Querying student policy by slug: {}", slug);
        StudentPolicy entity = policyRepo.findByPolicySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Student policy", "slug", slug));
        return mapper.toPolicyResponse(entity);
    }

    // ==================== Mitigation Forms ====================

    @Override
    public List<MitigationFormResponse> getAllMitigationForms() {
        log.debug("Querying all active mitigation forms");
        List<MitigationForm> entities = mitigationRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByFormNameAsc();
        log.info("Retrieved {} mitigation forms", entities.size());
        return mapper.toMitigationSummaryResponseList(entities);
    }

    @Override
    public MitigationFormResponse getMitigationFormBySlug(String slug) {
        log.debug("Querying mitigation form by slug: {}", slug);
        MitigationForm entity = mitigationRepo.findByFormSlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Mitigation form", "slug", slug));
        return mapper.toMitigationResponse(entity);
    }

    // ==================== Staff ====================

    @Override
    public List<StaffResponse> getAllStaffCategories() {
        log.debug("Querying all active staff categories");
        List<StaffCategory> entities = staffRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();
        log.info("Retrieved {} staff categories", entities.size());
        return entities.stream()
                .map(e -> StaffResponse.builder()
                        .categoryName(e.getCategoryName())
                        .categorySlug(e.getCategorySlug())
                        .description(e.getDescription())
                        .build())
                .toList();
    }

    @Override
    public StaffResponse getStaffBySlug(String slug) {
        log.debug("Querying staff category by slug: {}", slug);
        StaffCategory entity = staffRepo.findByCategorySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Staff category", "slug", slug));
        return mapStaffEntity(entity);
    }

    // ==================== Info ====================

    @Override
    public List<InfoResponse> getAllInfoCategories() {
        log.debug("Querying all active info categories");
        List<InfoCategory> entities = infoRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();
        log.info("Retrieved {} info categories", entities.size());
        return entities.stream()
                .map(e -> InfoResponse.builder()
                        .categoryName(e.getCategoryName())
                        .categorySlug(e.getCategorySlug())
                        .description(e.getDescription())
                        .build())
                .toList();
    }

    @Override
    public InfoResponse getInfoBySlug(String slug) {
        log.debug("Querying info category by slug: {}", slug);
        InfoCategory entity = infoRepo.findByCategorySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Info category", "slug", slug));
        return mapInfoEntity(entity);
    }

    // ========================================================================================
    // PRIVATE MAPPING HELPERS  (for entities with contentJson)
    // ========================================================================================

    private Map<String, Object> parseContentJson(String contentJson) {
        return contentJson != null
                ? JsonUtils.fromJsonSafe(contentJson, new TypeReference<>() {})
                : Collections.emptyMap();
    }

    private FoundationProgramResponse mapFoundationEntity(FoundationCategory e) {
        Map<String, Object> json = parseContentJson(e.getContentJson());

        FoundationProgramResponse.FoundationProgramResponseBuilder b = FoundationProgramResponse.builder()
                .id(e.getId())
                .categoryName(e.getCategoryName())
                .categorySlug(e.getCategorySlug())
                .description(e.getDescription())
                .academicYear(e.getAcademicYear())
                .semester(e.getSemester())
                .effectiveFrom(e.getEffectiveFrom())
                .programName(e.getProgramName())
                .duration(e.getDuration())
                .totalCredits(e.getTotalCredits())
                .calendarFileUrl(e.getCalendarFileUrl())
                .specificationFileUrl(e.getSpecificationFileUrl())
                .timetableFileUrl(e.getTimetableFileUrl())
                .scheduleFileUrl(e.getScheduleFileUrl())
                .formFileUrl(e.getFormFileUrl())
                .lmsName(e.getLmsName())
                .lmsUrl(e.getLmsUrl())
                .loginInstructions(e.getLoginInstructions())
                .usernameFormat(e.getUsernameFormat())
                .defaultPasswordInfo(e.getDefaultPasswordInfo())
                .passwordResetUrl(e.getPasswordResetUrl())
                .additionalNotes(e.getAdditionalNotes())
                .formName(e.getFormName())
                .submissionEmail(e.getSubmissionEmail())
                .submissionDeadline(e.getSubmissionDeadline())
                .browserRequirements(e.getBrowserRequirements())
                .eligibleCircumstances(e.getEligibleCircumstances())
                .requiredEvidence(e.getRequiredEvidence())
                .lastUpdated(mapper.formatDateTime(e.getUpdatedAt()));

        if (json != null) {
            b.events(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("events")),
                    new TypeReference<List<AcademicCalendarResponse.CalendarEventResponse>>() {}));
            b.modules(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("modules")),
                    new TypeReference<List<FoundationProgramResponse.FoundationModuleResponse>>() {}));
            b.contacts(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("contacts")),
                    new TypeReference<List<FoundationProgramResponse.ContactResponse>>() {}));
            b.schedule(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("schedule")),
                    new TypeReference<List<FoundationProgramResponse.ScheduleDayResponse>>() {}));
            b.assessments(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("assessments")),
                    new TypeReference<List<FoundationProgramResponse.AssessmentResponse>>() {}));
            b.supportContact(JsonUtils.fromJsonOrNull(JsonUtils.toJsonSafe(json.get("supportContact")),
                    FoundationProgramResponse.ContactResponse.class));
            b.contactPerson(JsonUtils.fromJsonOrNull(JsonUtils.toJsonSafe(json.get("contactPerson")),
                    FoundationProgramResponse.ContactResponse.class));
        }

        return b.build();
    }

    private StudentsRelationsUnitResponse mapSruRoot(SruCategory root) {
        List<StudentsRelationsUnitResponse.CategorySummaryResponse> categories = sruRepo.findAll().stream()
                .filter(s -> !s.getCategorySlug().equals("_root") && !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(s -> StudentsRelationsUnitResponse.CategorySummaryResponse.builder()
                        .categoryName(s.getCategoryName())
                        .categorySlug(s.getCategorySlug())
                        .description(s.getDescription())
                        .build())
                .toList();

        return StudentsRelationsUnitResponse.builder()
                .unitName(root.getUnitName())
                .description(root.getDescription())
                .location(root.getLocation())
                .email(root.getEmail())
                .phone(root.getPhone())
                .officeHours(root.getOfficeHours())
                .categories(categories)
                .build();
    }

    private StudentsRelationsUnitResponse mapSruCategory(SruCategory entity) {
        return StudentsRelationsUnitResponse.builder()
                .categoryName(entity.getCategoryName())
                .description(entity.getDescription())
                .videos(mapper.toSruVideoResponseList(entity.getVideos()))
                .lastUpdated(mapper.formatDateTime(entity.getUpdatedAt()))
                .build();
    }

    private StaffResponse mapStaffEntity(StaffCategory e) {
        Map<String, Object> json = parseContentJson(e.getContentJson());

        StaffResponse.StaffResponseBuilder b = StaffResponse.builder()
                .categoryName(e.getCategoryName())
                .categorySlug(e.getCategorySlug())
                .description(e.getDescription())
                .departmentFullName(e.getDepartmentFullName())
                .lastUpdated(mapper.formatDateTime(e.getUpdatedAt()));

        if (json != null) {
            b.staffMembers(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("staffMembers")),
                    new TypeReference<List<StaffResponse.StaffMemberResponse>>() {}));
            b.generalInfo(JsonUtils.fromJsonOrNull(JsonUtils.toJsonSafe(json.get("generalInfo")),
                    StaffResponse.GeneralInfoResponse.class));
            b.departments(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("departments")),
                    new TypeReference<List<StaffResponse.DepartmentResponse>>() {}));
            b.mailGroups(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("mailGroups")),
                    new TypeReference<List<StaffResponse.MailGroupResponse>>() {}));
            b.documents(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("documents")),
                    new TypeReference<List<StaffResponse.DocumentResponse>>() {}));
            b.emergencyContacts(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("emergencyContacts")),
                    new TypeReference<Map<String, String>>() {}));
        }

        return b.build();
    }

    private InfoResponse mapInfoEntity(InfoCategory e) {
        Map<String, Object> json = parseContentJson(e.getContentJson());

        InfoResponse.InfoResponseBuilder b = InfoResponse.builder()
                .categoryName(e.getCategoryName())
                .categorySlug(e.getCategorySlug())
                .description(e.getDescription())
                .lastUpdated(mapper.formatDateTime(e.getUpdatedAt()));

        if (json != null) {
            b.programmeCategories(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("programmeCategories")),
                    new TypeReference<List<InfoResponse.ProgrammeCategoryResponse>>() {}));
            b.admissionsContact(JsonUtils.fromJsonOrNull(JsonUtils.toJsonSafe(json.get("admissionsContact")),
                    InfoResponse.AdmissionsContactResponse.class));
            b.houses(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("houses")),
                    new TypeReference<List<InfoResponse.HouseResponse>>() {}));
            b.currentAcademicYear(json.get("currentAcademicYear") != null ? json.get("currentAcademicYear").toString() : null);
            b.office(json.get("office") != null ? json.get("office").toString() : null);
            b.email(json.get("email") != null ? json.get("email").toString() : null);
            b.phone(json.get("phone") != null ? json.get("phone").toString() : null);
            b.socialMedia(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("socialMedia")),
                    new TypeReference<Map<String, String>>() {}));
            b.currentOffice(JsonUtils.fromJsonOrNull(JsonUtils.toJsonSafe(json.get("currentOffice")),
                    InfoResponse.CurrentOfficeResponse.class));
            b.upcomingEvents(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("upcomingEvents")),
                    new TypeReference<List<InfoResponse.UpcomingEventResponse>>() {}));
            b.totalClubs(json.get("totalClubs") != null ? ((Number) json.get("totalClubs")).intValue() : null);
            b.clubs(JsonUtils.fromJsonSafe(JsonUtils.toJsonSafe(json.get("clubs")),
                    new TypeReference<List<InfoResponse.ClubInfoResponse>>() {}));
            b.joinInstructions(json.get("joinInstructions") != null ? json.get("joinInstructions").toString() : null);
        }

        return b.build();
    }
}

