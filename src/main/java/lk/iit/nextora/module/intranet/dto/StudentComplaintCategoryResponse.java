package lk.iit.nextora.module.intranet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for student complaint categories.
 * Used for both summary (list) and detail (by-slug) endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentComplaintCategoryResponse {
    private Long id;
    private String categoryName;
    private String categorySlug;
    private String description;
    private String formUrl;
    private String contactEmail;
    private String contactPhone;
    private String instructions;
    private Integer responseTimeBusinessDays;
    private Boolean isActive;
    private String lastUpdated;
}

