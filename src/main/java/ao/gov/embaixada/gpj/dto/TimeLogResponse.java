package ao.gov.embaixada.gpj.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TimeLogResponse(
        UUID id,
        UUID taskId,
        String userId,
        Double hours,
        String description,
        LocalDate logDate,
        Instant createdAt
) {}
