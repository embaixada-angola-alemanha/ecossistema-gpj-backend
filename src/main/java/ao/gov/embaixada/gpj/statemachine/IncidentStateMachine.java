package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.IncidentStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;

import java.util.Map;
import java.util.Set;

public class IncidentStateMachine {

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED_TRANSITIONS = Map.of(
            IncidentStatus.OPEN, Set.of(
                    IncidentStatus.INVESTIGATING,
                    IncidentStatus.CLOSED
            ),
            IncidentStatus.INVESTIGATING, Set.of(
                    IncidentStatus.IDENTIFIED,
                    IncidentStatus.MONITORING,
                    IncidentStatus.RESOLVED,
                    IncidentStatus.CLOSED
            ),
            IncidentStatus.IDENTIFIED, Set.of(
                    IncidentStatus.MONITORING,
                    IncidentStatus.RESOLVED,
                    IncidentStatus.CLOSED
            ),
            IncidentStatus.MONITORING, Set.of(
                    IncidentStatus.RESOLVED,
                    IncidentStatus.INVESTIGATING,
                    IncidentStatus.CLOSED
            ),
            IncidentStatus.RESOLVED, Set.of(
                    IncidentStatus.CLOSED,
                    IncidentStatus.OPEN
            ),
            IncidentStatus.CLOSED, Set.of()
    );

    private IncidentStateMachine() {
        // Utility class
    }

    public static boolean isTransitionAllowed(IncidentStatus from, IncidentStatus to) {
        Set<IncidentStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static void validateTransition(IncidentStatus from, IncidentStatus to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException("Incident", from.name(), to.name());
        }
    }
}
