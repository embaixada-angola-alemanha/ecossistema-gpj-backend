package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.MaintenanceStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceStateMachineTest {

    @ParameterizedTest
    @CsvSource({
            "SCHEDULED, IN_PROGRESS",
            "SCHEDULED, CANCELLED",
            "IN_PROGRESS, COMPLETED",
            "IN_PROGRESS, CANCELLED"
    })
    void validTransitions_shouldBeAllowed(MaintenanceStatus from, MaintenanceStatus to) {
        assertTrue(MaintenanceStateMachine.isTransitionAllowed(from, to));
        assertDoesNotThrow(() -> MaintenanceStateMachine.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "SCHEDULED, COMPLETED",
            "SCHEDULED, SCHEDULED",
            "IN_PROGRESS, SCHEDULED",
            "IN_PROGRESS, IN_PROGRESS",
            "COMPLETED, SCHEDULED",
            "COMPLETED, IN_PROGRESS",
            "COMPLETED, CANCELLED",
            "COMPLETED, COMPLETED",
            "CANCELLED, SCHEDULED",
            "CANCELLED, IN_PROGRESS",
            "CANCELLED, COMPLETED",
            "CANCELLED, CANCELLED"
    })
    void invalidTransitions_shouldNotBeAllowed(MaintenanceStatus from, MaintenanceStatus to) {
        assertFalse(MaintenanceStateMachine.isTransitionAllowed(from, to));
    }

    @Test
    void invalidTransition_shouldThrowException() {
        InvalidStateTransitionException ex = assertThrows(
                InvalidStateTransitionException.class,
                () -> MaintenanceStateMachine.validateTransition(MaintenanceStatus.COMPLETED, MaintenanceStatus.IN_PROGRESS));

        assertTrue(ex.getMessage().contains("MaintenanceWindow"));
        assertTrue(ex.getMessage().contains("COMPLETED"));
        assertTrue(ex.getMessage().contains("IN_PROGRESS"));
    }

    @Test
    void completedState_isTerminal() {
        for (MaintenanceStatus target : MaintenanceStatus.values()) {
            assertFalse(MaintenanceStateMachine.isTransitionAllowed(MaintenanceStatus.COMPLETED, target));
        }
    }

    @Test
    void cancelledState_isTerminal() {
        for (MaintenanceStatus target : MaintenanceStatus.values()) {
            assertFalse(MaintenanceStateMachine.isTransitionAllowed(MaintenanceStatus.CANCELLED, target));
        }
    }
}
