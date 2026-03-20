package lk.iit.nextora.module.intranet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MitigationFormResponse {
    private Long id;
    private String formName;
    private String formSlug;
    private String university;
    private String description;
    private String formFileUrl;
    private String submissionEmail;
    private String submissionDeadline;
    private List<String> eligibleCircumstances;
    private List<String> requiredDocuments;
    private List<String> limitations;
    private List<String> possibleOutcomes;
    private Integer processingTimeBusinessDays;
    private String extensionDuration;
    private String deferralDetails;
    private FoundationProgramResponse.ContactResponse contactPerson;
    private String lastUpdated;
}

