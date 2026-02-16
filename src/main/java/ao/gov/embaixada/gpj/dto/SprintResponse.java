package ao.gov.embaixada.gpj.dto;

import ao.gov.embaixada.gpj.enums.SprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SprintResponse(
        UUID id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        SprintStatus status,
        Double capacityHours,
        int taskCount,
        Instant createdAt,
        Instant updatedAt,
        String createdBy
) {}
