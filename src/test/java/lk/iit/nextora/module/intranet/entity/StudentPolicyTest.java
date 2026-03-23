package lk.iit.nextora.module.intranet.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StudentPolicy Entity Unit Tests")
class StudentPolicyTest {

    @Nested
    @DisplayName("StudentPolicy Builder")
    class StudentPolicyBuilderTests {

        @Test
        @DisplayName("Should create policy with all fields")
        void buildPolicy_withAllFields_success() {
            // When
            StudentPolicy policy = StudentPolicy.builder()
                    .id(1L)
                    .policyName("Code of Conduct")
                    .policySlug("code-of-conduct")
                    .version("2.0")
                    .effectiveDate("2026-03-22")
                    .description("Student conduct guidelines")
                    .policyContent("Content about conduct rules")
                    .policyFileUrl("https://example.com/policy.pdf")
                    .contactName("Policy Officer")
                    .contactRole("Head of Student Affairs")
                    .contactEmail("officer@example.com")
                    .isActive(true)
                    .build();

            // Then
            assertThat(policy).isNotNull();
            assertThat(policy.getId()).isEqualTo(1L);
            assertThat(policy.getPolicyName()).isEqualTo("Code of Conduct");
            assertThat(policy.getPolicySlug()).isEqualTo("code-of-conduct");
            assertThat(policy.getVersion()).isEqualTo("2.0");
            assertThat(policy.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should create policy with minimal fields")
        void buildPolicy_minimal_success() {
            // When
            StudentPolicy policy = StudentPolicy.builder()
                    .policyName("Attendance Policy")
                    .policySlug("attendance-policy")
                    .build();

            // Then
            assertThat(policy).isNotNull();
            assertThat(policy.getPolicyName()).isEqualTo("Attendance Policy");
            assertThat(policy.getPolicySlug()).isEqualTo("attendance-policy");
        }
    }

    @Nested
    @DisplayName("StudentPolicy Validation")
    class StudentPolicyValidationTests {

        @Test
        @DisplayName("Should validate required fields are populated")
        void validateRequiredFields() {
            // When
            StudentPolicy policy = StudentPolicy.builder()
                    .id(1L)
                    .policyName("Dress Code")
                    .policySlug("dress-code")
                    .version("1.0")
                    .build();

            // Then
            assertThat(policy.getPolicyName()).isNotNull();
            assertThat(policy.getPolicyName()).isNotEmpty();
            assertThat(policy.getPolicySlug()).isNotNull();
            assertThat(policy.getPolicySlug()).isNotEmpty();
            assertThat(policy.getVersion()).isNotNull();
        }

        @Test
        @DisplayName("Should handle optional fields correctly")
        void validateOptionalFields() {
            // When
            StudentPolicy policy = StudentPolicy.builder()
                    .policyName("Academic Integrity")
                    .policySlug("academic-integrity")
                    .description("Guidelines on academic honesty")
                    .contactEmail("integrity@university.edu")
                    .build();

            // Then
            assertThat(policy.getDescription()).isEqualTo("Guidelines on academic honesty");
            assertThat(policy.getContactEmail()).isEqualTo("integrity@university.edu");
        }
    }
}
