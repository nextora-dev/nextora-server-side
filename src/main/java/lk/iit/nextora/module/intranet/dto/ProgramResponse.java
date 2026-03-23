package lk.iit.nextora.module.intranet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for undergraduate and postgraduate programme information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgramResponse {

    private Long id;
    private String programCode;
    private String programName;
    private String programSlug;
    private String awardingUniversity;
    private String duration;
    private Integer totalCredits;
    private String description;
    private String entryRequirements;
    private List<String> careerProspects;
    private List<ProgramModuleResponse> modules;
    private String programSpecificationUrl;
    private String handbookUrl;
    private Boolean isActive;
    private String lastUpdated;

    /**
     * Inner DTO for programme module details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramModuleResponse {
        private Long id;
        private Integer year;
        private Integer semester;
        private String moduleCode;
        private String moduleName;
        private Integer credits;
        private Boolean isCore;
    }
}
