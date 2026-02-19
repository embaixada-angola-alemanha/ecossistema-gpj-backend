package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.DeploymentCreateRequest;
import ao.gov.embaixada.gpj.dto.DeploymentResponse;
import ao.gov.embaixada.gpj.entity.Deployment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeploymentMapper {

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.displayName")
    @Mapping(target = "environment", expression = "java(entity.getEnvironment() != null ? entity.getEnvironment().name() : null)")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    DeploymentResponse toResponse(Deployment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "deployedBy", ignore = true)
    @Mapping(target = "deployedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "environment", expression = "java(request.environment() != null ? ao.gov.embaixada.gpj.enums.DeploymentEnvironment.valueOf(request.environment()) : null)")
    Deployment toEntity(DeploymentCreateRequest request);
}
