package lk.iit.nextora.module.intranet.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.common.util.JsonUtils;
import lk.iit.nextora.module.intranet.dto.*;
import lk.iit.nextora.module.intranet.entity.*;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * MapStruct mapper for all intranet module entity → response DTO conversions.
 */
@Mapper(config = MapperConfiguration.class)
public interface IntranetMapper {

    // ==================== Schedule Category ====================

    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "events", source = "events", qualifiedByName = "mapScheduleEvents")
    ScheduleCategoryResponse toScheduleCategoryResponse(ScheduleCategory entity);

    @Named("scheduleSummary")
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "events", ignore = true)
    ScheduleCategoryResponse toScheduleCategorySummaryResponse(ScheduleCategory entity);

    @IterableMapping(qualifiedByName = "scheduleSummary")
    List<ScheduleCategoryResponse> toScheduleCategorySummaryResponseList(List<ScheduleCategory> entities);

    @Mapping(target = "isActive", source = "isEventActive")
    ScheduleCategoryResponse.ScheduleEventResponse toScheduleEventResponse(ScheduleEvent entity);

    List<ScheduleCategoryResponse.ScheduleEventResponse> toScheduleEventResponseList(List<ScheduleEvent> entities);

    @Named("mapScheduleEvents")
    default List<ScheduleCategoryResponse.ScheduleEventResponse> mapScheduleEvents(List<ScheduleEvent> events) {
        if (events == null) return null;
        return toScheduleEventResponseList(events);
    }

    // ==================== Student Complaint Category ====================

    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    StudentComplaintCategoryResponse toComplaintResponse(StudentComplaintCategory entity);

    @Named("complaintSummary")
    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "formUrl", ignore = true)
    @Mapping(target = "contactEmail", ignore = true)
    @Mapping(target = "contactPhone", ignore = true)
    @Mapping(target = "instructions", ignore = true)
    @Mapping(target = "responseTimeBusinessDays", ignore = true)
    StudentComplaintCategoryResponse toComplaintSummaryResponse(StudentComplaintCategory entity);

    @IterableMapping(qualifiedByName = "complaintSummary")
    List<StudentComplaintCategoryResponse> toComplaintSummaryResponseList(List<StudentComplaintCategory> entities);

    // ==================== Academic Calendar ====================

    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "events", source = "events", qualifiedByName = "mapCalendarEvents")
    AcademicCalendarResponse toCalendarResponse(AcademicCalendar entity);

    @Named("calendarSummary")
    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "events", ignore = true)
    AcademicCalendarResponse toCalendarSummaryResponse(AcademicCalendar entity);

    @IterableMapping(qualifiedByName = "calendarSummary")
    List<AcademicCalendarResponse> toCalendarSummaryResponseList(List<AcademicCalendar> entities);

    AcademicCalendarResponse.CalendarEventResponse toCalendarEventResponse(CalendarEvent entity);

    List<AcademicCalendarResponse.CalendarEventResponse> toCalendarEventResponseList(List<CalendarEvent> entities);

    @Named("mapCalendarEvents")
    default List<AcademicCalendarResponse.CalendarEventResponse> mapCalendarEvents(List<CalendarEvent> events) {
        if (events == null) return null;
        return toCalendarEventResponseList(events);
    }

    // ==================== Program (UG / PG) ====================

    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "modules", source = "modules", qualifiedByName = "mapProgramModules")
    @Mapping(target = "careerProspects", source = "careerProspects")
    ProgramResponse toProgramResponse(Program entity);

    @Named("programSummary")
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "careerProspects", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "entryRequirements", ignore = true)
    @Mapping(target = "totalCredits", ignore = true)
    @Mapping(target = "programSpecificationUrl", ignore = true)
    @Mapping(target = "handbookUrl", ignore = true)
    ProgramResponse toProgramSummaryResponse(Program entity);

    @IterableMapping(qualifiedByName = "programSummary")
    List<ProgramResponse> toProgramSummaryResponseList(List<Program> entities);

    @Mapping(target = "isCore", source = "isCore")
    ProgramResponse.ProgramModuleResponse toProgramModuleResponse(ProgramModule entity);

    List<ProgramResponse.ProgramModuleResponse> toProgramModuleResponseList(List<ProgramModule> entities);

    @Named("mapProgramModules")
    default List<ProgramResponse.ProgramModuleResponse> mapProgramModules(List<ProgramModule> modules) {
        if (modules == null) return null;
        return toProgramModuleResponseList(modules);
    }

    // ==================== Student Policy ====================

    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "contactPerson", source = ".", qualifiedByName = "mapPolicyContact")
    @Mapping(target = "keyPoints", source = "keyPoints")
    @Mapping(target = "disciplinaryProcess", source = "disciplinaryProcess")
    StudentPolicyResponse toPolicyResponse(StudentPolicy entity);

    @Named("policySummary")
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "policyContent", ignore = true)
    @Mapping(target = "policyFileUrl", ignore = true)
    @Mapping(target = "keyPoints", ignore = true)
    @Mapping(target = "disciplinaryProcess", ignore = true)
    @Mapping(target = "contactPerson", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "effectiveDate", ignore = true)
    StudentPolicyResponse toPolicySummaryResponse(StudentPolicy entity);

    @IterableMapping(qualifiedByName = "policySummary")
    List<StudentPolicyResponse> toPolicySummaryResponseList(List<StudentPolicy> entities);

    @Named("mapPolicyContact")
    default FoundationProgramResponse.ContactResponse mapPolicyContact(StudentPolicy entity) {
        if (entity.getContactName() == null && entity.getContactEmail() == null) return null;
        return FoundationProgramResponse.ContactResponse.builder()
                .name(entity.getContactName())
                .role(entity.getContactRole())
                .email(entity.getContactEmail())
                .build();
    }

    // ==================== Mitigation Form ====================

    @Mapping(target = "lastUpdated", source = "updatedAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "contactPerson", source = ".", qualifiedByName = "mapMitigationContact")
    MitigationFormResponse toMitigationResponse(MitigationForm entity);

    @Named("mitigationSummary")
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "formFileUrl", ignore = true)
    @Mapping(target = "submissionEmail", ignore = true)
    @Mapping(target = "submissionDeadline", ignore = true)
    @Mapping(target = "eligibleCircumstances", ignore = true)
    @Mapping(target = "requiredDocuments", ignore = true)
    @Mapping(target = "limitations", ignore = true)
    @Mapping(target = "possibleOutcomes", ignore = true)
    @Mapping(target = "processingTimeBusinessDays", ignore = true)
    @Mapping(target = "extensionDuration", ignore = true)
    @Mapping(target = "deferralDetails", ignore = true)
    @Mapping(target = "contactPerson", ignore = true)
    MitigationFormResponse toMitigationSummaryResponse(MitigationForm entity);

    @IterableMapping(qualifiedByName = "mitigationSummary")
    List<MitigationFormResponse> toMitigationSummaryResponseList(List<MitigationForm> entities);

    @Named("mapMitigationContact")
    default FoundationProgramResponse.ContactResponse mapMitigationContact(MitigationForm entity) {
        if (entity.getContactName() == null && entity.getContactEmail() == null) return null;
        return FoundationProgramResponse.ContactResponse.builder()
                .name(entity.getContactName())
                .email(entity.getContactEmail())
                .phone(entity.getContactPhone())
                .build();
    }

    // ==================== SRU Video ====================

    StudentsRelationsUnitResponse.VideoResponse toSruVideoResponse(SruVideo entity);

    List<StudentsRelationsUnitResponse.VideoResponse> toSruVideoResponseList(List<SruVideo> entities);

    // ==================== Timestamp helper ====================

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Named("parseJsonList")
    default List<String> parseJsonList(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return List.of();
        return JsonUtils.fromJsonSafe(jsonStr, new TypeReference<>() {
        });
    }
}
