package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;

public record IncidentResolveRequest(
        String rootCause,
        @NotBlank String resolution
) {
}
