package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;

public record MonitoredServiceCreateRequest(
        @NotBlank String name,
        @NotBlank String displayName,
        String type,
        String healthUrl,
        String metadata
) {
}
