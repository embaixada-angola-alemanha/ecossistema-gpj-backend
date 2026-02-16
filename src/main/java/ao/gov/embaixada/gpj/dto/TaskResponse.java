package ao.gov.embaixada.gpj.dto;

import ao.gov.embaixada.gpj.enums.TaskPriority;
import ao.gov.embaixada.gpj.enums.TaskStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        String assignee,
        Double estimatedHours,
        Double consumedHours,
        Integer progressPct,
        UUID sprintId,
        String sprintTitle,
        List<UUID> dependencyIds,
        Instant createdAt,
        Instant updatedAt,
        String createdBy
) {}
