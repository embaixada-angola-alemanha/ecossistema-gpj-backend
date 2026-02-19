package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.UUID;

public record MonitoredServiceResponse(
        UUID id,
        String name,
        String displayName,
        String type,
        String status,
        Instant lastCheckAt,
        Long responseTimeMs,
        int consecutiveFailures,
        Instant createdAt
) {
}
