package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class TaskStateMachine {

    private static final Map<TaskStatus, Set<TaskStatus>> TRANSITIONS = new EnumMap<>(TaskStatus.class);

    static {
        TRANSITIONS.put(TaskStatus.BACKLOG, Set.of(
                TaskStatus.TODO, TaskStatus.BLOCKED, TaskStatus.CANCELLED));
        TRANSITIONS.put(TaskStatus.TODO, Set.of(
                TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.CANCELLED));
        TRANSITIONS.put(TaskStatus.IN_PROGRESS, Set.of(
                TaskStatus.IN_REVIEW, TaskStatus.TODO, TaskStatus.BLOCKED, TaskStatus.CANCELLED));
        TRANSITIONS.put(TaskStatus.IN_REVIEW, Set.of(
                TaskStatus.DONE, TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.CANCELLED));
        TRANSITIONS.put(TaskStatus.DONE, Set.of());
        TRANSITIONS.put(TaskStatus.BLOCKED, Set.of(
                TaskStatus.BACKLOG, TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW, TaskStatus.CANCELLED));
        TRANSITIONS.put(TaskStatus.CANCELLED, Set.of());
    }

    public boolean isTransitionAllowed(TaskStatus from, TaskStatus to) {
        Set<TaskStatus> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(TaskStatus from, TaskStatus to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException(
                    "Task", from.name(), to.name());
        }
    }
}
