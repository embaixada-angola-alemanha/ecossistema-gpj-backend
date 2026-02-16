package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MilestoneCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull LocalDate targetDate
) {}
