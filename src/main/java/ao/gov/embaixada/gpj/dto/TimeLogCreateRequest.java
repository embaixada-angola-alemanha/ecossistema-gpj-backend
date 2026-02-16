package ao.gov.embaixada.gpj.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TimeLogCreateRequest(
        @NotNull @Positive Double hours,
        String description,
        @NotNull LocalDate logDate
) {}
