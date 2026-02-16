package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.MilestoneCreateRequest;
import ao.gov.embaixada.gpj.dto.MilestoneResponse;
import ao.gov.embaixada.gpj.entity.Milestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MilestoneMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sprint", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Milestone toEntity(MilestoneCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sprint", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(MilestoneCreateRequest request, @MappingTarget Milestone milestone);

    @Mapping(target = "sprintId", source = "sprint.id")
    @Mapping(target = "sprintTitle", source = "sprint.title")
    MilestoneResponse toResponse(Milestone milestone);
}
