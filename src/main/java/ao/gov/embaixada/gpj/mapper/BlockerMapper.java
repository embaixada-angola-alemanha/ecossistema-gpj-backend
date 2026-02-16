package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.BlockerCreateRequest;
import ao.gov.embaixada.gpj.dto.BlockerResponse;
import ao.gov.embaixada.gpj.entity.Blocker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BlockerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resolution", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Blocker toEntity(BlockerCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resolution", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(BlockerCreateRequest request, @MappingTarget Blocker blocker);

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    BlockerResponse toResponse(Blocker blocker);
}
