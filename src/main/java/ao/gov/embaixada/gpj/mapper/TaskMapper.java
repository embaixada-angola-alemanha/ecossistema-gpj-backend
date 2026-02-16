package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.TaskCreateRequest;
import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.dto.TaskUpdateRequest;
import ao.gov.embaixada.gpj.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "consumedHours", ignore = true)
    @Mapping(target = "progressPct", ignore = true)
    @Mapping(target = "sprint", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "timeLogs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Task toEntity(TaskCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "consumedHours", ignore = true)
    @Mapping(target = "sprint", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "timeLogs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(TaskUpdateRequest request, @MappingTarget Task task);

    @Mapping(target = "sprintId", expression = "java(task.getSprint() != null ? task.getSprint().getId() : null)")
    @Mapping(target = "sprintTitle", expression = "java(task.getSprint() != null ? task.getSprint().getTitle() : null)")
    @Mapping(target = "dependencyIds", expression = "java(mapDependencyIds(task))")
    TaskResponse toResponse(Task task);

    default List<UUID> mapDependencyIds(Task task) {
        if (task.getDependencies() == null) {
            return List.of();
        }
        return task.getDependencies().stream()
                .map(Task::getId)
                .collect(Collectors.toList());
    }
}
