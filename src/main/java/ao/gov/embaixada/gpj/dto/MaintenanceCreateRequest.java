package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MaintenanceCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull Instant scheduledStart,
        @NotNull Instant scheduledEnd,
        List<UUID> affectedServiceIds
) {
}
