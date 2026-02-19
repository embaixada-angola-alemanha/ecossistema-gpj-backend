package ao.gov.embaixada.gpj.statemachine;

import ao.gov.embaixada.gpj.enums.MaintenanceStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;

import java.util.Map;
import java.util.Set;

public class MaintenanceStateMachine {

    private static final Map<MaintenanceStatus, Set<MaintenanceStatus>> ALLOWED_TRANSITIONS = Map.of(
            MaintenanceStatus.SCHEDULED, Set.of(
                    MaintenanceStatus.IN_PROGRESS,
                    MaintenanceStatus.CANCELLED
            ),
            MaintenanceStatus.IN_PROGRESS, Set.of(
                    MaintenanceStatus.COMPLETED,
                    MaintenanceStatus.CANCELLED
            ),
            MaintenanceStatus.COMPLETED, Set.of(),
            MaintenanceStatus.CANCELLED, Set.of()
    );

    private MaintenanceStateMachine() {
        // Utility class
    }

    public static boolean isTransitionAllowed(MaintenanceStatus from, MaintenanceStatus to) {
        Set<MaintenanceStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static void validateTransition(MaintenanceStatus from, MaintenanceStatus to) {
        if (!isTransitionAllowed(from, to)) {
            throw new InvalidStateTransitionException("MaintenanceWindow", from.name(), to.name());
        }
    }
}
