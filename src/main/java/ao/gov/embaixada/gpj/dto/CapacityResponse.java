package ao.gov.embaixada.gpj.dto;

import java.util.UUID;

public record CapacityResponse(
        UUID sprintId,
        String sprintTitle,
        Double capacityHours,
        Double allocatedHours,
        Double remainingHours,
        Double consumedHours,
        double utilizationPct
) {}
