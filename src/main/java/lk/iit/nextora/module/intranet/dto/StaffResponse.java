package lk.iit.nextora.module.intranet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffResponse {
    private String categoryName;
    private String categorySlug;
    private String description;
    private String departmentFullName;
    private List<StaffMemberResponse> staffMembers;
    private GeneralInfoResponse generalInfo;
    private List<DepartmentResponse> departments;
    private List<MailGroupResponse> mailGroups;
    private List<DocumentResponse> documents;
    private Map<String, String> emergencyContacts;
    private String lastUpdated;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StaffMemberResponse {
        private Long id;
        private String name;
        private String designation;
        private String email;
        private String phone;
        private String office;
        private String specialization;
        private String officeHours;
        private String extension;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GeneralInfoResponse {
        private String institutionName;
        private String mainAddress;
        private String mainPhone;
        private String mainEmail;
        private String website;
        private String workingHours;
        private String academicYear;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DepartmentResponse {
        private String departmentName;
        private String headOfDepartment;
        private String email;
        private String phone;
        private List<StaffMemberResponse> contacts;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MailGroupResponse {
        private String groupName;
        private String email;
        private String description;
        private String accessLevel;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DocumentResponse {
        private Long id;
        private String documentName;
        private String category;
        private String fileUrl;
        private String fileType;
        private Integer fileSizeKb;
        private String uploadedDate;
    }
}

