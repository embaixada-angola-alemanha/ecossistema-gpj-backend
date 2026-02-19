package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.MonitoredServiceCreateRequest;
import ao.gov.embaixada.gpj.dto.MonitoredServiceResponse;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonitoredServiceMapper {

    @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    MonitoredServiceResponse toResponse(MonitoredService entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastCheckAt", ignore = true)
    @Mapping(target = "responseTimeMs", ignore = true)
    @Mapping(target = "consecutiveFailures", ignore = true)
    @Mapping(target = "healthChecks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "type", expression = "java(request.type() != null ? ao.gov.embaixada.gpj.enums.ServiceType.valueOf(request.type()) : null)")
    MonitoredService toEntity(MonitoredServiceCreateRequest request);
}
