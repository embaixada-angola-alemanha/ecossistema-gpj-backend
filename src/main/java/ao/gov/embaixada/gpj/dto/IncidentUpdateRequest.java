package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;

public record IncidentUpdateRequest(
        @NotBlank String message
) {
}
