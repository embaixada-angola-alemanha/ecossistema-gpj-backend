package ao.gov.embaixada.gpj.dto;

import java.util.List;
import java.util.Map;

public record ProjectReportResponse(
        long totalSprints,
        long activeSprints,
        long totalTasks,
        Map<String, Long> tasksByStatus,
        double totalHoursPlanned,
        double totalHoursConsumed,
        double overallProgressPct,
        long activeBlockers,
        long completedMilestones,
        long totalMilestones,
        List<VelocityResponse> velocityHistory,
        List<CapacityResponse> capacityUtilization
) {}
