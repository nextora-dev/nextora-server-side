package lk.iit.nextora.module.intranet.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Program Entity Unit Tests")
class ProgramTest {

    @Nested
    @DisplayName("Builder and Initialization")
    class BuilderTests {

        @Test
        @DisplayName("Should create Program with all fields")
        void builder_withAllFields_createsValidProgram() {
            String programCode = "CS101";
            String programName = "Bachelor of Computer Science";
            String programSlug = "bsc-computer-science";
            String level = "UNDERGRADUATE";
            int totalCredits = 120;

            Program program = Program.builder()
                    .programCode(programCode)
                    .programName(programName)
                    .programSlug(programSlug)
                    .programLevel(level)
                    .awardingUniversity("University of Colombo")
                    .duration("3 years")
                    .totalCredits(totalCredits)
                    .description("A comprehensive CS program")
                    .entryRequirements("A-levels")
                    .build();

            assertThat(program).isNotNull();
            assertThat(program.getProgramCode()).isEqualTo(programCode);
            assertThat(program.getProgramName()).isEqualTo(programName);
            assertThat(program.getProgramSlug()).isEqualTo(programSlug);
            assertThat(program.getProgramLevel()).isEqualTo(level);
            assertThat(program.getTotalCredits()).isEqualTo(totalCredits);
        }

        @Test
        @DisplayName("Should set empty collections by default")
        void builder_defaultCollections() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .build();

            assertThat(program.getCareerProspects()).isNotNull().isEmpty();
            assertThat(program.getModules()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Program Level")
    class ProgramLevelTests {

        @Test
        @DisplayName("Should support UNDERGRADUATE level")
        void supportUndergraduateLevel() {
            Program program = Program.builder()
                    .programCode("UG101")
                    .programName("UG Program")
                    .programSlug("ug-program")
                    .programLevel("UNDERGRADUATE")
                    .build();

            assertThat(program.getProgramLevel()).isEqualTo("UNDERGRADUATE");
        }

        @Test
        @DisplayName("Should support POSTGRADUATE level")
        void supportPostgraduateLevel() {
            Program program = Program.builder()
                    .programCode("PG101")
                    .programName("PG Program")
                    .programSlug("pg-program")
                    .programLevel("POSTGRADUATE")
                    .build();

            assertThat(program.getProgramLevel()).isEqualTo("POSTGRADUATE");
        }
    }

    @Nested
    @DisplayName("Career Prospects")
    class CareerProspectsTests {

        @Test
        @DisplayName("Should add career prospects")
        void addCareerProspects() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .careerProspects(new ArrayList<>())
                    .build();

            program.getCareerProspects().add("Software Engineer");
            program.getCareerProspects().add("Data Scientist");

            assertThat(program.getCareerProspects())
                    .hasSize(2)
                    .contains("Software Engineer", "Data Scientist");
        }

        @Test
        @DisplayName("Should maintain multiple career prospects")
        void multipleCareerProspects() {
            List<String> prospects = new ArrayList<>();
            prospects.add("Developer");
            prospects.add("Analyst");
            prospects.add("Architect");

            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .careerProspects(prospects)
                    .build();

            assertThat(program.getCareerProspects()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Program Modules")
    class ModulesTests {

        @Test
        @DisplayName("Should add module to program")
        void addModule() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .build();

            ProgramModule module = ProgramModule.builder()
                    .moduleCode("CS201")
                    .moduleName("Data Structures")
                    .year(1)
                    .semester(1)
                    .build();

            program.addModule(module);

            assertThat(program.getModules()).hasSize(1);
            assertThat(program.getModules().get(0)).isEqualTo(module);
            assertThat(module.getProgram()).isEqualTo(program);
        }

        @Test
        @DisplayName("Should maintain multiple modules")
        void multipleModules() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .build();

            for (int i = 0; i < 5; i++) {
                ProgramModule module = ProgramModule.builder()
                        .moduleCode("CS" + (200 + i))
                        .moduleName("Module " + i)
                        .year(1)
                        .semester(1)
                        .build();
                program.addModule(module);
            }

            assertThat(program.getModules()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Program URLs")
    class UrlsTests {

        @Test
        @DisplayName("Should store specification URL")
        void storeSpecificationUrl() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .programSpecificationUrl("https://example.com/spec.pdf")
                    .build();

            assertThat(program.getProgramSpecificationUrl())
                    .isEqualTo("https://example.com/spec.pdf");
        }

        @Test
        @DisplayName("Should store handbook URL")
        void storeHandbookUrl() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .handbookUrl("https://example.com/handbook.pdf")
                    .build();

            assertThat(program.getHandbookUrl()).isEqualTo("https://example.com/handbook.pdf");
        }
    }

    @Nested
    @DisplayName("University Information")
    class UniversityTests {

        @Test
        @DisplayName("Should store awarding university")
        void storeUniversity() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .awardingUniversity("University of Colombo")
                    .build();

            assertThat(program.getAwardingUniversity())
                    .isEqualTo("University of Colombo");
        }

        @Test
        @DisplayName("Should store program duration")
        void storeDuration() {
            Program program = Program.builder()
                    .programCode("CS101")
                    .programName("CS Program")
                    .programSlug("cs-program")
                    .duration("3 years")
                    .build();

            assertThat(program.getDuration()).isEqualTo("3 years");
        }
    }

    @Nested
    @DisplayName("Program Slug Uniqueness")
    class SlugTests {

        @Test
        @DisplayName("Should have unique slug")
        void uniqueSlug() {
            Program program1 = Program.builder()
                    .programCode("CS101")
                    .programName("Computer Science")
                    .programSlug("bsc-cs")
                    .build();

            Program program2 = Program.builder()
                    .programCode("CS102")
                    .programName("Computer Science Advanced")
                    .programSlug("bsc-cs-advanced")
                    .build();

            assertThat(program1.getProgramSlug())
                    .isNotEqualTo(program2.getProgramSlug());
        }
    }
}

