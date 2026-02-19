package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.HealthCheckLogResponse;
import ao.gov.embaixada.gpj.entity.HealthCheckLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HealthCheckLogMapper {

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    HealthCheckLogResponse toResponse(HealthCheckLog entity);
}
