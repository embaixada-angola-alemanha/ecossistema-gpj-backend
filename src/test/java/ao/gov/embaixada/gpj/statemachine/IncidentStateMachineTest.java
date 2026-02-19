package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.IncidentStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class IncidentStateMachineTest {

    @ParameterizedTest
    @CsvSource({
            "OPEN, INVESTIGATING",
            "OPEN, CLOSED",
            "INVESTIGATING, IDENTIFIED",
            "INVESTIGATING, MONITORING",
            "INVESTIGATING, RESOLVED",
            "INVESTIGATING, CLOSED",
            "IDENTIFIED, MONITORING",
            "IDENTIFIED, RESOLVED",
            "IDENTIFIED, CLOSED",
            "MONITORING, RESOLVED",
            "MONITORING, INVESTIGATING",
            "MONITORING, CLOSED",
            "RESOLVED, CLOSED",
            "RESOLVED, OPEN"
    })
    void validTransitions_shouldBeAllowed(IncidentStatus from, IncidentStatus to) {
        assertTrue(IncidentStateMachine.isTransitionAllowed(from, to));
        assertDoesNotThrow(() -> IncidentStateMachine.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN, IDENTIFIED",
            "OPEN, MONITORING",
            "OPEN, RESOLVED",
            "OPEN, OPEN",
            "INVESTIGATING, OPEN",
            "INVESTIGATING, INVESTIGATING",
            "IDENTIFIED, OPEN",
            "IDENTIFIED, INVESTIGATING",
            "IDENTIFIED, IDENTIFIED",
            "MONITORING, OPEN",
            "MONITORING, IDENTIFIED",
            "MONITORING, MONITORING",
            "RESOLVED, INVESTIGATING",
            "RESOLVED, IDENTIFIED",
            "RESOLVED, MONITORING",
            "RESOLVED, RESOLVED",
            "CLOSED, OPEN",
            "CLOSED, INVESTIGATING",
            "CLOSED, IDENTIFIED",
            "CLOSED, MONITORING",
            "CLOSED, RESOLVED",
            "CLOSED, CLOSED"
    })
    void invalidTransitions_shouldNotBeAllowed(IncidentStatus from, IncidentStatus to) {
        assertFalse(IncidentStateMachine.isTransitionAllowed(from, to));
    }

    @Test
    void invalidTransition_shouldThrowException() {
        InvalidStateTransitionException ex = assertThrows(
                InvalidStateTransitionException.class,
                () -> IncidentStateMachine.validateTransition(IncidentStatus.OPEN, IncidentStatus.RESOLVED));

        assertTrue(ex.getMessage().contains("Incident"));
        assertTrue(ex.getMessage().contains("OPEN"));
        assertTrue(ex.getMessage().contains("RESOLVED"));
    }

    @Test
    void closedState_isTerminal() {
        for (IncidentStatus target : IncidentStatus.values()) {
            assertFalse(IncidentStateMachine.isTransitionAllowed(IncidentStatus.CLOSED, target));
        }
    }
}
