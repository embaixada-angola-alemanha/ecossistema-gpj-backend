package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SprintStateMachineTest {

    private final SprintStateMachine stateMachine = new SprintStateMachine();

    @Test
    void planningToActive_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(SprintStatus.PLANNING, SprintStatus.ACTIVE));
    }

    @Test
    void planningToCancelled_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(SprintStatus.PLANNING, SprintStatus.CANCELLED));
    }

    @Test
    void activeToCompleted_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(SprintStatus.ACTIVE, SprintStatus.COMPLETED));
    }

    @Test
    void activeToCancelled_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(SprintStatus.ACTIVE, SprintStatus.CANCELLED));
    }

    @Test
    void completedToAny_isNotAllowed() {
        assertFalse(stateMachine.isTransitionAllowed(SprintStatus.COMPLETED, SprintStatus.ACTIVE));
        assertFalse(stateMachine.isTransitionAllowed(SprintStatus.COMPLETED, SprintStatus.PLANNING));
        assertFalse(stateMachine.isTransitionAllowed(SprintStatus.COMPLETED, SprintStatus.CANCELLED));
    }

    @Test
    void cancelledToAny_isNotAllowed() {
        assertFalse(stateMachine.isTransitionAllowed(SprintStatus.CANCELLED, SprintStatus.ACTIVE));
        assertFalse(stateMachine.isTransitionAllowed(SprintStatus.CANCELLED, SprintStatus.PLANNING));
    }

    @Test
    void planningToCompleted_isNotAllowed() {
        assertFalse(stateMachine.isTransitionAllowed(SprintStatus.PLANNING, SprintStatus.COMPLETED));
    }

    @Test
    void validateTransition_validDoesNotThrow() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(SprintStatus.PLANNING, SprintStatus.ACTIVE));
    }

    @Test
    void validateTransition_invalidThrows() {
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(SprintStatus.COMPLETED, SprintStatus.ACTIVE));
    }
}
