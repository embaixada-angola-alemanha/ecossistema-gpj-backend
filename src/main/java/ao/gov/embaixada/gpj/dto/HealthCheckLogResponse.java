package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.UUID;

public record HealthCheckLogResponse(
        UUID id,
        UUID serviceId,
        Instant checkedAt,
        String status,
        Long responseTimeMs,
        String errorMessage
) {
}
