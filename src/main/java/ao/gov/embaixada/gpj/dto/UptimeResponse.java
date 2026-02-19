package ao.gov.embaixada.gpj.dto;

import java.util.UUID;

public record UptimeResponse(
        UUID serviceId,
        String serviceName,
        double uptime24h,
        double uptime7d,
        double uptime30d
) {
}
