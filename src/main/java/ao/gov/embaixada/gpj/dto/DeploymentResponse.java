package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.UUID;

public record DeploymentResponse(
        UUID id,
        UUID serviceId,
        String serviceName,
        String versionTag,
        String commitHash,
        String environment,
        String deployedBy,
        Instant deployedAt,
        String status,
        String notes,
        Instant createdAt
) {
}
