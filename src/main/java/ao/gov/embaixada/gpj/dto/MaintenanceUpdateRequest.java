package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MaintenanceUpdateRequest(
        String title,
        String description,
        Instant scheduledStart,
        Instant scheduledEnd,
        List<UUID> affectedServiceIds
) {
}
