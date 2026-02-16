package ao.gov.embaixada.gpj.dto;

import ao.gov.embaixada.gpj.enums.BlockerStatus;

import java.time.Instant;
import java.util.UUID;

public record BlockerResponse(
        UUID id,
        UUID taskId,
        String taskTitle,
        String title,
        String description,
        String severity,
        BlockerStatus status,
        String resolution,
        Instant resolvedAt,
        Instant createdAt,
        String createdBy
) {}
