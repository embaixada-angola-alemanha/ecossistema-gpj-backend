package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskDependencyRequest(
        @NotNull UUID dependsOnId
) {}
