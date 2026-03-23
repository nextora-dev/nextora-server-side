package lk.iit.nextora.module.kuppi.scheduler;

import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.infrastructure.notification.service.KuppiNotificationService;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import lk.iit.nextora.module.kuppi.repository.KuppiSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiSessionStatusScheduler Unit Tests")
class KuppiSessionStatusSchedulerTest {

    @Mock private KuppiSessionRepository sessionRepository;
    @Mock private KuppiNotificationService kuppiNotificationService;

    @InjectMocks
    private KuppiSessionStatusScheduler scheduler;

    // ============================================================
    // SCHEDULED → LIVE transition
    // ============================================================

    @Nested
    @DisplayName("Transition SCHEDULED → LIVE")
    class ScheduledToLiveTests {

        @Test
        @DisplayName("Should transition sessions to LIVE when start time reached")
        void updateSessionStatuses_scheduledToLive_transitions() {
            // Given
            KuppiSession session = KuppiSession.builder()
                    .id(1L).title("Math Kuppi").subject("Math")
                    .status(KuppiSessionStatus.SCHEDULED)
                    .liveLink("https://meet.google.com/abc")
                    .build();
            when(sessionRepository.findSessionsStartingBetween(any(), any(), any()))
                    .thenReturn(List.of());
            when(sessionRepository.findSessionsToGoLive(eq(KuppiSessionStatus.SCHEDULED), any()))
                    .thenReturn(List.of(session));
            when(sessionRepository.findSessionsToComplete(any(), any())).thenReturn(List.of());
            when(sessionRepository.findMissedScheduledSessions(any(), any())).thenReturn(List.of());

            // When
            scheduler.updateSessionStatuses();

            // Then
            assertThat(session.getStatus()).isEqualTo(KuppiSessionStatus.LIVE);
            verify(sessionRepository).save(session);
            verify(kuppiNotificationService).notifyKuppiSessionLive(
                    eq(1L), eq("Math Kuppi"), eq("Math"), eq("https://meet.google.com/abc"));
        }
    }

    // ============================================================
    // LIVE → COMPLETED transition
    // ============================================================

    @Nested
    @DisplayName("Transition LIVE → COMPLETED")
    class LiveToCompletedTests {

        @Test
        @DisplayName("Should transition sessions to COMPLETED when end time passed")
        void updateSessionStatuses_liveToCompleted_transitions() {
            // Given
            KuppiSession session = KuppiSession.builder()
                    .id(2L).title("Physics Kuppi").subject("Physics")
                    .status(KuppiSessionStatus.LIVE).build();
            when(sessionRepository.findSessionsStartingBetween(any(), any(), any()))
                    .thenReturn(List.of());
            when(sessionRepository.findSessionsToGoLive(any(), any())).thenReturn(List.of());
            when(sessionRepository.findSessionsToComplete(eq(KuppiSessionStatus.LIVE), any()))
                    .thenReturn(List.of(session));
            when(sessionRepository.findMissedScheduledSessions(any(), any())).thenReturn(List.of());

            // When
            scheduler.updateSessionStatuses();

            // Then
            assertThat(session.getStatus()).isEqualTo(KuppiSessionStatus.COMPLETED);
            verify(sessionRepository).save(session);
            verify(kuppiNotificationService).notifyKuppiSessionCompleted(
                    eq(2L), eq("Physics Kuppi"), eq("Physics"));
        }
    }

    // ============================================================
    // Missed sessions (SCHEDULED → COMPLETED)
    // ============================================================

    @Nested
    @DisplayName("Missed Sessions")
    class MissedSessionsTests {

        @Test
        @DisplayName("Should mark missed sessions as COMPLETED")
        void updateSessionStatuses_missedSessions_marksCompleted() {
            // Given
            KuppiSession session = KuppiSession.builder()
                    .id(3L).title("Missed Kuppi")
                    .status(KuppiSessionStatus.SCHEDULED).build();
            when(sessionRepository.findSessionsStartingBetween(any(), any(), any()))
                    .thenReturn(List.of());
            when(sessionRepository.findSessionsToGoLive(any(), any())).thenReturn(List.of());
            when(sessionRepository.findSessionsToComplete(any(), any())).thenReturn(List.of());
            when(sessionRepository.findMissedScheduledSessions(eq(KuppiSessionStatus.SCHEDULED), any()))
                    .thenReturn(List.of(session));

            // When
            scheduler.updateSessionStatuses();

            // Then
            assertThat(session.getStatus()).isEqualTo(KuppiSessionStatus.COMPLETED);
            verify(sessionRepository).save(session);
        }
    }

    // ============================================================
    // Session reminders
    // ============================================================

    @Nested
    @DisplayName("Session Reminders")
    class SessionReminderTests {

        @Test
        @DisplayName("Should send reminders for sessions starting in ~30 minutes")
        void updateSessionStatuses_sessionsStartingSoon_sendsReminders() {
            // Given
            KuppiSession session = KuppiSession.builder()
                    .id(4L).title("Upcoming Kuppi").subject("Chemistry")
                    .liveLink("https://zoom.us/j/123")
                    .status(KuppiSessionStatus.SCHEDULED).build();
            when(sessionRepository.findSessionsStartingBetween(
                    eq(KuppiSessionStatus.SCHEDULED), any(), any()))
                    .thenReturn(List.of(session));
            when(sessionRepository.findSessionsToGoLive(any(), any())).thenReturn(List.of());
            when(sessionRepository.findSessionsToComplete(any(), any())).thenReturn(List.of());
            when(sessionRepository.findMissedScheduledSessions(any(), any())).thenReturn(List.of());

            // When
            scheduler.updateSessionStatuses();

            // Then
            verify(kuppiNotificationService).notifyKuppiSessionReminder(
                    eq(4L), eq("Upcoming Kuppi"), eq("Chemistry"),
                    eq("https://zoom.us/j/123"), eq(30));
        }
    }

    // ============================================================
    // No transitions needed
    // ============================================================

    @Nested
    @DisplayName("No Transitions")
    class NoTransitionsTests {

        @Test
        @DisplayName("Should handle no sessions needing transition")
        void updateSessionStatuses_noTransitions_noop() {
            // Given
            when(sessionRepository.findSessionsStartingBetween(any(), any(), any()))
                    .thenReturn(List.of());
            when(sessionRepository.findSessionsToGoLive(any(), any())).thenReturn(List.of());
            when(sessionRepository.findSessionsToComplete(any(), any())).thenReturn(List.of());
            when(sessionRepository.findMissedScheduledSessions(any(), any())).thenReturn(List.of());

            // When
            scheduler.updateSessionStatuses();

            // Then
            verify(sessionRepository, never()).save(any());
            verify(kuppiNotificationService, never()).notifyKuppiSessionLive(any(), any(), any(), any());
        }
    }

    // ============================================================
    // Error handling
    // ============================================================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle exceptions gracefully without propagating")
        void updateSessionStatuses_exception_doesNotPropagate() {
            // Given
            when(sessionRepository.findSessionsStartingBetween(any(), any(), any()))
                    .thenThrow(new RuntimeException("DB connection failed"));

            // When & Then - should not throw
            scheduler.updateSessionStatuses();
        }
    }

    // ============================================================
    // triggerStatusUpdate
    // ============================================================

    @Nested
    @DisplayName("triggerStatusUpdate")
    class TriggerStatusUpdateTests {

        @Test
        @DisplayName("Should delegate to updateSessionStatuses")
        void triggerStatusUpdate_delegatesToMainMethod() {
            // Given
            when(sessionRepository.findSessionsStartingBetween(any(), any(), any()))
                    .thenReturn(List.of());
            when(sessionRepository.findSessionsToGoLive(any(), any())).thenReturn(List.of());
            when(sessionRepository.findSessionsToComplete(any(), any())).thenReturn(List.of());
            when(sessionRepository.findMissedScheduledSessions(any(), any())).thenReturn(List.of());

            // When
            scheduler.triggerStatusUpdate();

            // Then
            verify(sessionRepository).findSessionsToGoLive(any(), any());
        }
    }

    // ============================================================
    // Multiple transitions in single run
    // ============================================================

    @Nested
    @DisplayName("Multiple Transitions")
    class MultipleTransitionsTests {

        @Test
        @DisplayName("Should handle all transition types in a single run")
        void updateSessionStatuses_multipleTransitions_handlesAll() {
            // Given
            KuppiSession goLive = KuppiSession.builder()
                    .id(1L).title("GoLive").subject("S1")
                    .status(KuppiSessionStatus.SCHEDULED)
                    .liveLink("link1").build();
            KuppiSession complete = KuppiSession.builder()
                    .id(2L).title("Complete").subject("S2")
                    .status(KuppiSessionStatus.LIVE).build();
            KuppiSession missed = KuppiSession.builder()
                    .id(3L).title("Missed")
                    .status(KuppiSessionStatus.SCHEDULED).build();

            when(sessionRepository.findSessionsStartingBetween(any(), any(), any()))
                    .thenReturn(List.of());
            when(sessionRepository.findSessionsToGoLive(any(), any())).thenReturn(List.of(goLive));
            when(sessionRepository.findSessionsToComplete(any(), any())).thenReturn(List.of(complete));
            when(sessionRepository.findMissedScheduledSessions(any(), any())).thenReturn(List.of(missed));

            // When
            scheduler.updateSessionStatuses();

            // Then
            assertThat(goLive.getStatus()).isEqualTo(KuppiSessionStatus.LIVE);
            assertThat(complete.getStatus()).isEqualTo(KuppiSessionStatus.COMPLETED);
            assertThat(missed.getStatus()).isEqualTo(KuppiSessionStatus.COMPLETED);
            verify(sessionRepository, times(3)).save(any());
        }
    }
}
