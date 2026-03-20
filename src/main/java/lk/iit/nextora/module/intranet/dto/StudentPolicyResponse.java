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
public class StudentPolicyResponse {
    private Long id;
    private String policyName;
    private String policySlug;
    private String version;
    private String effectiveDate;
    private String description;
    private String policyContent;
    private String policyFileUrl;
    private List<String> keyPoints;
    private List<String> disciplinaryProcess;
    private FoundationProgramResponse.ContactResponse contactPerson;
    private String lastUpdated;
}

