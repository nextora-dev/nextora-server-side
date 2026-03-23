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

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UndergraduateController Unit Tests")
class UndergraduateControllerTest {

    @Mock private IntranetContentService intranetService;

    @InjectMocks private UndergraduateController controller;

    @Nested
    @DisplayName("getAll")
    class GetAllUndergraduateProgramsTests {

        @Test
        @DisplayName("Should return all undergraduate programs")
        void getAll_success() {
            ProgramResponse program = ProgramResponse.builder()
                    .id(1L)
                    .programCode("CS101")
                    .programName("Bachelor of Computer Science")
                    .build();

            when(intranetService.getAllUndergraduatePrograms())
                    .thenReturn(List.of(program));

            ApiResponse<List<ProgramResponse>> response = controller.getAll();

            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(1);
            verify(intranetService).getAllUndergraduatePrograms();
        }

        @Test
        @DisplayName("Should return empty list when no programs exist")
        void getAll_empty() {
            when(intranetService.getAllUndergraduatePrograms())
                    .thenReturn(List.of());

            ApiResponse<List<ProgramResponse>> response = controller.getAll();

            assertThat(response).isNotNull();
            assertThat(response.getData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBySlug")
    class GetUndergraduateProgramBySlugTests {

        @Test
        @DisplayName("Should get undergraduate program by slug")
        void getBySlug_success() {
            String slug = "bsc-computer-science";
            ProgramResponse program = ProgramResponse.builder()
                    .id(1L)
                    .programSlug(slug)
                    .programName("Bachelor of Computer Science")
                    .build();

            when(intranetService.getUndergraduateProgramBySlug(slug))
                    .thenReturn(program);

            ApiResponse<ProgramResponse> response = controller.getBySlug(slug);

            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getProgramSlug()).isEqualTo(slug);
            verify(intranetService).getUndergraduateProgramBySlug(slug);
        }

        @Test
        @DisplayName("Should propagate exception when program not found")
        void getBySlug_notFound() {
            String slug = "nonexistent";
            when(intranetService.getUndergraduateProgramBySlug(slug))
                    .thenThrow(new RuntimeException("Program not found"));

            assertThatThrownBy(() -> controller.getBySlug(slug))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
