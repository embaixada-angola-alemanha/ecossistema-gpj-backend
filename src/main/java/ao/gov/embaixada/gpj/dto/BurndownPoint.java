package ao.gov.embaixada.gpj.dto;

import java.time.LocalDate;

public record BurndownPoint(
        LocalDate date,
        double remainingHours,
        double idealHours
) {}
