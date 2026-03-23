package lk.iit.nextora.common.enums;

import lombok.Getter;

/**
 * Enum representing the type/purpose of meeting
 *
 * GENERAL - Regular club meeting open to all members
 * EXECUTIVE - Meeting for club executives/leaders only
 * EMERGENCY - Urgent meeting called for important matters
 * ANNUAL_GENERAL - Yearly formal meeting (AGM)
 * SPECIAL - Special purpose meeting
 * ACADEMIC_GUIDANCE - Discussion about academic matters, course selection
 * PROJECT_DISCUSSION - Discussion about projects, assignments, thesis
 * CAREER_COUNSELING - Career advice and guidance
 * PERSONAL_CONSULTATION - Personal matters affecting studies
 * RESEARCH_DISCUSSION - Research-related discussions
 * OTHER - Any other type of meeting
 */
@Getter
public enum MeetingType {
    GENERAL("General Meeting"),
    EXECUTIVE("Executive Meeting"),
    EMERGENCY("Emergency Meeting"),
    ANNUAL_GENERAL("Annual General Meeting"),
    SPECIAL("Special Meeting"),
    ACADEMIC_GUIDANCE("Academic Guidance"),
    PROJECT_DISCUSSION("Project Discussion"),
    CAREER_COUNSELING("Career Counseling"),
    PERSONAL_CONSULTATION("Personal Consultation"),
    RESEARCH_DISCUSSION("Research Discussion"),
    OTHER("Other");

    private final String displayName;

    MeetingType(String displayName) {
        this.displayName = displayName;
    }
}
