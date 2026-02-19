package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.UUID;

public record IncidentUpdateResponse(
        UUID id,
        String message,
        String author,
        Instant createdAt
) {
}
