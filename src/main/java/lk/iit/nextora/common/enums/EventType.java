package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the type of an event
 */
@Getter
@RequiredArgsConstructor
public enum EventType {

    WORKSHOP("Workshop", "Hands-on learning session"),
    SEMINAR("Seminar", "Lecture or presentation"),
    HACKATHON("Hackathon", "Coding competition or challenge"),
    SOCIAL("Social", "Social gathering or meetup"),
    SPORTS("Sports", "Sports event or competition"),
    CULTURAL("Cultural", "Cultural event or celebration"),
    ACADEMIC("Academic", "Academic event or conference"),
    OTHER("Other", "Other type of event");

    private final String displayName;
    private final String description;
}
