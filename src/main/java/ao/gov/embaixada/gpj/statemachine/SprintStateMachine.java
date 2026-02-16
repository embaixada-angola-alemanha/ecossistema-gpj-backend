package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class SprintStateMachine {

    private static final Map<SprintStatus, Set<SprintStatus>> TRANSITIONS = new EnumMap<>(SprintStatus.class);

    static {
        TRANSITIONS.put(SprintStatus.PLANNING, Set.of(SprintStatus.ACTIVE, SprintStatus.CANCELLED));
        TRANSITIONS.put(SprintStatus.ACTIVE, Set.of(SprintStatus.COMPLETED, SprintStatus.CANCELLED));
        TRANSITIONS.put(SprintStatus.COMPLETED, Set.of());
        TRANSITIONS.put(SprintStatus.CANCELLED, Set.of());
    }

    public boolean isTransitionAllowed(SprintStatus from, SprintStatus to) {
        Set<SprintStatus> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validateTransition(SprintStatus from, SprintStatus to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException(
                    "Sprint", from.name(), to.name());
        }
    }
}
