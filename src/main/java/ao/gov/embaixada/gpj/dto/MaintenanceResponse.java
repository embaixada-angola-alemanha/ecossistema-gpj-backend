package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id,
        String title,
        String description,
        Instant scheduledStart,
        Instant scheduledEnd,
        Instant actualStart,
        Instant actualEnd,
        String status,
        List<MonitoredServiceResponse> affectedServices,
        String createdByUser,
        Instant createdAt
) {
}
