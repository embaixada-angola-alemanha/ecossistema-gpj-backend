package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.IncidentCreateRequest;
import ao.gov.embaixada.gpj.dto.IncidentResponse;
import ao.gov.embaixada.gpj.entity.Incident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MonitoredServiceMapper.class, IncidentUpdateMapper.class})
public interface IncidentMapper {

    @Mapping(target = "severity", expression = "java(entity.getSeverity() != null ? entity.getSeverity().name() : null)")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "affectedServices", source = "affectedServices")
    @Mapping(target = "updates", source = "updates")
    IncidentResponse toResponse(Incident entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "rootCause", ignore = true)
    @Mapping(target = "resolution", ignore = true)
    @Mapping(target = "affectedServices", ignore = true)
    @Mapping(target = "updates", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "severity", expression = "java(request.severity() != null ? ao.gov.embaixada.gpj.enums.IncidentSeverity.valueOf(request.severity()) : null)")
    @Mapping(target = "assignedTo", source = "assignedTo")
    Incident toEntity(IncidentCreateRequest request);
}
