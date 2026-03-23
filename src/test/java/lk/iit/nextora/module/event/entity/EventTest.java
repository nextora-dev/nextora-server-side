package lk.iit.nextora.module.event.entity;

import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Event Entity Unit Tests")
class EventTest {

    // ============================================================
    // VALID TIME RANGE TESTS
    // ============================================================

    @Nested
    @DisplayName("hasValidTimeRange")
    class HasValidTimeRangeTests {

        @Test
        @DisplayName("Should return true when start is before end")
        void hasValidTimeRange_validRange_returnsTrue() {
            // Given
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .build();

            // When
            boolean result = event.hasValidTimeRange();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when start is after end")
        void hasValidTimeRange_invalidRange_returnsFalse() {
            // Given
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            LocalDateTime start = end.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .build();

            // When
            boolean result = event.hasValidTimeRange();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when start or end is null")
        void hasValidTimeRange_nullDates_returnsFalse() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(null)
                    .endAt(null)
                    .build();

            // When
            boolean result = event.hasValidTimeRange();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // CAN EDIT TESTS
    // ============================================================

    @Nested
    @DisplayName("canEdit")
    class CanEditTests {

        @Test
        @DisplayName("Should return true when status is DRAFT")
        void canEdit_draftStatus_returnsTrue() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .status(EventStatus.DRAFT)
                    .build();

            // When
            boolean result = event.canEdit();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when status is PUBLISHED")
        void canEdit_publishedStatus_returnsFalse() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .status(EventStatus.PUBLISHED)
                    .build();

            // When
            boolean result = event.canEdit();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is CANCELLED")
        void canEdit_cancelledStatus_returnsFalse() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .status(EventStatus.CANCELLED)
                    .build();

            // When
            boolean result = event.canEdit();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // IS VISIBLE TESTS
    // ============================================================

    @Nested
    @DisplayName("isVisible")
    class IsVisibleTests {

        @Test
        @DisplayName("Should return true when status is PUBLISHED")
        void isVisible_publishedStatus_returnsTrue() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .status(EventStatus.PUBLISHED)
                    .build();

            // When
            boolean result = event.isVisible();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true when status is COMPLETED")
        void isVisible_completedStatus_returnsTrue() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .status(EventStatus.COMPLETED)
                    .build();

            // When
            boolean result = event.isVisible();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when status is DRAFT")
        void isVisible_draftStatus_returnsFalse() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .status(EventStatus.DRAFT)
                    .build();

            // When
            boolean result = event.isVisible();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // IS UPCOMING TESTS
    // ============================================================

    @Nested
    @DisplayName("isUpcoming")
    class IsUpcomingTests {

        @Test
        @DisplayName("Should return true when event is in future and published")
        void isUpcoming_futureEventPublished_returnsTrue() {
            // Given
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.PUBLISHED)
                    .build();

            // When
            boolean result = event.isUpcoming();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when event is in past")
        void isUpcoming_pastEvent_returnsFalse() {
            // Given
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.PUBLISHED)
                    .build();

            // When
            boolean result = event.isUpcoming();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is not PUBLISHED")
        void isUpcoming_notPublished_returnsFalse() {
            // Given
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.DRAFT)
                    .build();

            // When
            boolean result = event.isUpcoming();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // IS ONGOING TESTS
    // ============================================================

    @Nested
    @DisplayName("isOngoing")
    class IsOngoingTests {

        @Test
        @DisplayName("Should return true when event is currently happening")
        void isOngoing_eventCurrentlyHappening_returnsTrue() {
            // Given
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now().plusHours(1);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.PUBLISHED)
                    .build();

            // When
            boolean result = event.isOngoing();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when event has ended")
        void isOngoing_eventEnded_returnsFalse() {
            // Given
            LocalDateTime start = LocalDateTime.now().minusHours(3);
            LocalDateTime end = LocalDateTime.now().minusHours(1);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.PUBLISHED)
                    .build();

            // When
            boolean result = event.isOngoing();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // REGISTRATION TESTS
    // ============================================================

    @Nested
    @DisplayName("isRegistrationOpen")
    class IsRegistrationOpenTests {

        @Test
        @DisplayName("Should return true when event is published and has capacity")
        void isRegistrationOpen_publishedWithCapacity_returnsTrue() {
            // Given
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.PUBLISHED)
                    .maxAttendees(100)
                    .registrations(new HashSet<>())
                    .build();

            // When
            boolean result = event.isRegistrationOpen();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when event is not published")
        void isRegistrationOpen_notPublished_returnsFalse() {
            // Given
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.DRAFT)
                    .maxAttendees(100)
                    .registrations(new HashSet<>())
                    .build();

            // When
            boolean result = event.isRegistrationOpen();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when event start time has passed")
        void isRegistrationOpen_eventStarted_returnsFalse() {
            // Given
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.PUBLISHED)
                    .maxAttendees(100)
                    .registrations(new HashSet<>())
                    .build();

            // When
            boolean result = event.isRegistrationOpen();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when no max attendees limit set")
        void isRegistrationOpen_noMaxAttendees_returnsTrue() {
            // Given
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            LocalDateTime end = start.plusHours(2);

            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .status(EventStatus.PUBLISHED)
                    .maxAttendees(null)
                    .registrations(new HashSet<>())
                    .build();

            // When
            boolean result = event.isRegistrationOpen();

            // Then
            assertThat(result).isTrue();
        }
    }

    // ============================================================
    // IS FULL TESTS
    // ============================================================

    @Nested
    @DisplayName("isFull")
    class IsFullTests {

        @Test
        @DisplayName("Should return false when under capacity")
        void isFull_underCapacity_returnsFalse() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .maxAttendees(100)
                    .registrations(new HashSet<>())
                    .build();

            // When
            boolean result = event.isFull();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when no max attendees limit")
        void isFull_noMaxAttendees_returnsFalse() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .maxAttendees(null)
                    .registrations(new HashSet<>())
                    .build();

            // When
            boolean result = event.isFull();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // VIEW COUNT TESTS
    // ============================================================

    @Nested
    @DisplayName("incrementViewCount")
    class IncrementViewCountTests {

        @Test
        @DisplayName("Should increment view count by 1")
        void incrementViewCount_success() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .viewCount(100L)
                    .build();

            // When
            event.incrementViewCount();

            // Then
            assertThat(event.getViewCount()).isEqualTo(101L);
        }

        @Test
        @DisplayName("Should increment from zero")
        void incrementViewCount_fromZero_success() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .viewCount(0L)
                    .build();

            // When
            event.incrementViewCount();

            // Then
            assertThat(event.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should handle multiple increments")
        void incrementViewCount_multiple_success() {
            // Given
            Event event = Event.builder()
                    .title("Tech Conference")
                    .viewCount(10L)
                    .build();

            // When
            event.incrementViewCount();
            event.incrementViewCount();
            event.incrementViewCount();

            // Then
            assertThat(event.getViewCount()).isEqualTo(13L);
        }
    }

    // ============================================================
    // EVENT BUILDER TESTS
    // ============================================================

    @Nested
    @DisplayName("Event Builder and Fields")
    class EventBuilderTests {

        @Test
        @DisplayName("Should create event with all fields")
        void eventBuilder_withAllFields_success() {
            // Given
            LocalDateTime start = LocalDateTime.of(2026, 6, 15, 9, 0);
            LocalDateTime end = LocalDateTime.of(2026, 6, 15, 17, 0);

            // When
            Event event = Event.builder()
                    .id(1L)
                    .title("Annual Tech Summit")
                    .description("A comprehensive tech summit")
                    .startAt(start)
                    .endAt(end)
                    .location("Convention Center")
                    .venue("Hall A")
                    .eventType(EventType.ACADEMIC)
                    .status(EventStatus.PUBLISHED)
                    .viewCount(500L)
                    .maxAttendees(1000)
                    .registrationLink("https://events.com/summit2026")
                    .coverImageUrl("https://s3.com/events/summit.jpg")
                    .build();

            // Then
            assertThat(event.getId()).isEqualTo(1L);
            assertThat(event.getTitle()).isEqualTo("Annual Tech Summit");
            assertThat(event.getDescription()).isEqualTo("A comprehensive tech summit");
            assertThat(event.getStartAt()).isEqualTo(start);
            assertThat(event.getEndAt()).isEqualTo(end);
            assertThat(event.getLocation()).isEqualTo("Convention Center");
            assertThat(event.getVenue()).isEqualTo("Hall A");
            assertThat(event.getEventType()).isEqualTo(EventType.ACADEMIC);
            assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
            assertThat(event.getViewCount()).isEqualTo(500L);
            assertThat(event.getMaxAttendees()).isEqualTo(1000);
        }

        @Test
        @DisplayName("Should create event with default values")
        void eventBuilder_withDefaults_success() {
            // Given
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            LocalDateTime end = start.plusHours(2);

            // When
            Event event = Event.builder()
                    .title("Tech Conference")
                    .startAt(start)
                    .endAt(end)
                    .build();

            // Then
            assertThat(event.getTitle()).isEqualTo("Tech Conference");
            assertThat(event.getEventType()).isEqualTo(EventType.OTHER);
            assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
            assertThat(event.getViewCount()).isEqualTo(0L);
            assertThat(event.getRegistrations()).isNotNull();
        }
    }
}

