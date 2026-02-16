package ao.gov.embaixada.gpj.dto;

import ao.gov.embaixada.gpj.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record TaskCreateRequest(
        @NotBlank String title,
        String description,
        TaskPriority priority,
        String assignee,
        @PositiveOrZero Double estimatedHours,
        UUID sprintId
) {}
