package ao.gov.embaixada.gpj.dto;

public record MonitoredServiceUpdateRequest(
        String displayName,
        String healthUrl,
        String metadata
) {
}
