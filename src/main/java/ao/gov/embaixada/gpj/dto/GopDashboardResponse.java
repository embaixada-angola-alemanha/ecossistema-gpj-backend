package ao.gov.embaixada.gpj.dto;

import java.util.List;

public record GopDashboardResponse(
        long servicesUp,
        long servicesDown,
        long servicesDegraded,
        long activeIncidents,
        long p1Incidents,
        List<DeploymentResponse> recentDeployments,
        List<MaintenanceResponse> upcomingMaintenance,
        long eventsToday
) {
}
