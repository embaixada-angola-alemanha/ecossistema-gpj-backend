package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record IncidentCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull String severity,
        List<UUID> affectedServiceIds,
        String assignedTo
) {
}
