package lk.iit.nextora.module.intranet.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.ProgramResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UndergraduateController Unit Tests")
class UndergraduateControllerTest {

    @Mock private IntranetContentService intranetService;

    @InjectMocks private UndergraduateController controller;

    @Nested
    @DisplayName("getAllUndergraduatePrograms")
    class GetAllUndergraduateProgramsTests {

        @Test
        @DisplayName("Should return all undergraduate programs")
        void getAllUndergraduatePrograms_success() {
            ProgramResponse program = ProgramResponse.builder()
                    .id(1L)
                    .programCode("CS101")
                    .programName("Bachelor of Computer Science")
                    .build();

            when(intranetService.getAllUndergraduatePrograms())
                    .thenReturn(List.of(program));

            ResponseEntity<ApiResponse<List<ProgramResponse>>> response = 
                    controller.getAllUndergraduatePrograms();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).hasSize(1);
            verify(intranetService).getAllUndergraduatePrograms();
        }

        @Test
        @DisplayName("Should return empty list when no programs exist")
        void getAllUndergraduatePrograms_empty() {
            when(intranetService.getAllUndergraduatePrograms())
                    .thenReturn(List.of());

            ResponseEntity<ApiResponse<List<ProgramResponse>>> response = 
                    controller.getAllUndergraduatePrograms();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUndergraduateProgramBySlug")
    class GetUndergraduateProgramBySlugTests {

        @Test
        @DisplayName("Should get undergraduate program by slug")
        void getUndergraduateProgramBySlug_success() {
            String slug = "bsc-computer-science";
            ProgramResponse program = ProgramResponse.builder()
                    .id(1L)
                    .programSlug(slug)
                    .programName("Bachelor of Computer Science")
                    .build();

            when(intranetService.getUndergraduateProgramBySlug(slug))
                    .thenReturn(program);

            ResponseEntity<ApiResponse<ProgramResponse>> response = 
                    controller.getUndergraduateProgramBySlug(slug);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getData()).isNotNull();
            assertThat(response.getBody().getData().getProgramSlug()).isEqualTo(slug);
            verify(intranetService).getUndergraduateProgramBySlug(slug);
        }

        @Test
        @DisplayName("Should propagate exception when program not found")
        void getUndergraduateProgramBySlug_notFound() {
            String slug = "nonexistent";
            when(intranetService.getUndergraduateProgramBySlug(slug))
                    .thenThrow(new RuntimeException("Program not found"));

            assertThatThrownBy(() -> controller.getUndergraduateProgramBySlug(slug))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
