package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeploymentCreateRequest(
        @NotNull UUID serviceId,
        @NotBlank String versionTag,
        String commitHash,
        @NotNull String environment,
        String notes
) {
}
