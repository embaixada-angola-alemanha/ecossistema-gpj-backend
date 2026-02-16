package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record SprintUpdateRequest(
        @NotBlank String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        @PositiveOrZero Double capacityHours
) {}
