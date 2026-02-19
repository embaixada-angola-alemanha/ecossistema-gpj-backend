package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.UUID;

public record SystemEventResponse(
        UUID id,
        UUID eventId,
        String source,
        String eventType,
        String entityType,
        String entityId,
        Instant timestamp,
        Instant receivedAt
) {
}
