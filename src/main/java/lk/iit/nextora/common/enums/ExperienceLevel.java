package lk.iit.nextora.common.enums;

import lombok.Getter;

/**
 * Enum representing the experience level for Kuppi Students
 */
@Getter
public enum ExperienceLevel {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced");

    private final String displayName;

    ExperienceLevel(String displayName) {
        this.displayName = displayName;
    }
}

