package ao.gov.embaixada.gpj.dto;

import java.util.Map;

public record DashboardResponse(
        long totalSprints,
        long totalTasks,
        long totalTimeLogs,
        double totalHoursLogged,
        Map<String, Long> tasksByStatus,
        Map<String, Long> sprintsByStatus
) {}
