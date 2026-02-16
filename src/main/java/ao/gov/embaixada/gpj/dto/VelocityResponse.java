package ao.gov.embaixada.gpj.dto;

import java.util.UUID;

public record VelocityResponse(
        UUID sprintId,
        String sprintTitle,
        long completedTasks,
        double completedHours,
        long totalTasks,
        long durationDays,
        double tasksPerDay,
        double hoursPerDay
) {}
