package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;

public record BlockerCreateRequest(
        @NotBlank String title,
        String description,
        String severity
) {}
