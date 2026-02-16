package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.TimeLogCreateRequest;
import ao.gov.embaixada.gpj.dto.TimeLogResponse;
import ao.gov.embaixada.gpj.entity.TimeLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TimeLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TimeLog toEntity(TimeLogCreateRequest request);

    @Mapping(target = "taskId", source = "task.id")
    TimeLogResponse toResponse(TimeLog timeLog);
}
