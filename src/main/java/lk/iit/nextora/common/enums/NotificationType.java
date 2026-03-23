package lk.iit.nextora.common.enums;

public enum NotificationType {
    GENERAL("General"),
    ANNOUNCEMENT("Announcement"),
    EVENT("Event"),
    VOTING_ALERT("Voting Alert"),
    SYSTEM("System"),
    SYSTEM_MESSAGE("System Message"),
    REMINDER("Reminder"),
    ALERT("Alert"),
    ASSIGNMENT("Assignment"),
    GRADE("Grade"),
    ATTENDANCE("Attendance"),
    MESSAGE("Message"),
    KUPPI_SESSION("Kuppi Session"),
    KUPPI_REMINDER("Kuppi Reminder"),
    EVENT_REMINDER("Event Reminder"),
    MEETING("Meeting"),
    MEETING_REMINDER("Meeting Reminder"),
    ELECTION("Election"),
    LOST_AND_FOUND("Lost & Found"),
    BOARDING_HOUSE("Boarding House");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
