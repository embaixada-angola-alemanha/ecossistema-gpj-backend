package ao.gov.embaixada.gpj.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BurndownResponse(
        UUID sprintId,
        String sprintTitle,
        LocalDate startDate,
        LocalDate endDate,
        double totalEstimatedHours,
        List<BurndownPoint> points
) {}
