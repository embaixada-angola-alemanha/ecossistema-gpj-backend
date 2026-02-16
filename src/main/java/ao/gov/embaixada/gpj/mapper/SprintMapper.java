package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.SprintCreateRequest;
import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.dto.SprintUpdateRequest;
import ao.gov.embaixada.gpj.entity.Sprint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SprintMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Sprint toEntity(SprintCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(SprintUpdateRequest request, @MappingTarget Sprint sprint);

    @Mapping(target = "taskCount", expression = "java(sprint.getTasks() != null ? sprint.getTasks().size() : 0)")
    SprintResponse toResponse(Sprint sprint);
}
