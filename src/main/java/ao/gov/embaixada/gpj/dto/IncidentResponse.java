package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        String title,
        String description,
        String severity,
        String status,
        List<MonitoredServiceResponse> affectedServices,
        String reportedBy,
        String assignedTo,
        Instant resolvedAt,
        String rootCause,
        String resolution,
        List<IncidentUpdateResponse> updates,
        Instant createdAt,
        Instant updatedAt
) {
}
