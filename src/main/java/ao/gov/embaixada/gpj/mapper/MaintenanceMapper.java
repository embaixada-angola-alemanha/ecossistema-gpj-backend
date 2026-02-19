package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.MaintenanceCreateRequest;
import ao.gov.embaixada.gpj.dto.MaintenanceResponse;
import ao.gov.embaixada.gpj.entity.MaintenanceWindow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MonitoredServiceMapper.class})
public interface MaintenanceMapper {

    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "affectedServices", source = "affectedServices")
    MaintenanceResponse toResponse(MaintenanceWindow entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actualStart", ignore = true)
    @Mapping(target = "actualEnd", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "affectedServices", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    MaintenanceWindow toEntity(MaintenanceCreateRequest request);
}
