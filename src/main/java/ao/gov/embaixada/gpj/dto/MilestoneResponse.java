package ao.gov.embaixada.gpj.dto;

import ao.gov.embaixada.gpj.enums.MilestoneStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MilestoneResponse(
        UUID id,
        UUID sprintId,
        String sprintTitle,
        String title,
        String description,
        LocalDate targetDate,
        MilestoneStatus status,
        Instant completedAt,
        Instant createdAt,
        String createdBy
) {}
