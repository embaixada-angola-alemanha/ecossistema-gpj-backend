package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskStateMachineTest {

    private final TaskStateMachine stateMachine = new TaskStateMachine();

    @Test
    void backlogToTodo_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BACKLOG, TaskStatus.TODO));
    }

    @Test
    void todoToInProgress_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
    }

    @Test
    void inProgressToInReview_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW));
    }

    @Test
    void inReviewToDone_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.IN_REVIEW, TaskStatus.DONE));
    }

    @Test
    void inReviewToInProgress_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.IN_REVIEW, TaskStatus.IN_PROGRESS));
    }

    @Test
    void anyToBlocked_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BACKLOG, TaskStatus.BLOCKED));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.TODO, TaskStatus.BLOCKED));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.IN_REVIEW, TaskStatus.BLOCKED));
    }

    @Test
    void blockedToStates_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BLOCKED, TaskStatus.BACKLOG));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BLOCKED, TaskStatus.TODO));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BLOCKED, TaskStatus.IN_REVIEW));
    }

    @Test
    void doneToAny_isNotAllowed() {
        assertFalse(stateMachine.isTransitionAllowed(TaskStatus.DONE, TaskStatus.BACKLOG));
        assertFalse(stateMachine.isTransitionAllowed(TaskStatus.DONE, TaskStatus.IN_PROGRESS));
        assertFalse(stateMachine.isTransitionAllowed(TaskStatus.DONE, TaskStatus.BLOCKED));
        assertFalse(stateMachine.isTransitionAllowed(TaskStatus.DONE, TaskStatus.CANCELLED));
    }

    @Test
    void cancelledToAny_isNotAllowed() {
        assertFalse(stateMachine.isTransitionAllowed(TaskStatus.CANCELLED, TaskStatus.BACKLOG));
        assertFalse(stateMachine.isTransitionAllowed(TaskStatus.CANCELLED, TaskStatus.TODO));
    }

    @Test
    void backlogToDone_isNotAllowed() {
        assertFalse(stateMachine.isTransitionAllowed(TaskStatus.BACKLOG, TaskStatus.DONE));
    }

    @Test
    void anyCancelledExceptDone_isAllowed() {
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BACKLOG, TaskStatus.CANCELLED));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.TODO, TaskStatus.CANCELLED));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.IN_REVIEW, TaskStatus.CANCELLED));
        assertTrue(stateMachine.isTransitionAllowed(TaskStatus.BLOCKED, TaskStatus.CANCELLED));
    }

    @Test
    void validateTransition_validDoesNotThrow() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(TaskStatus.BACKLOG, TaskStatus.TODO));
    }

    @Test
    void validateTransition_invalidThrows() {
        assertThrows(InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(TaskStatus.DONE, TaskStatus.BACKLOG));
    }
}
