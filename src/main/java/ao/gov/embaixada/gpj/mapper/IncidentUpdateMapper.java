package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.IncidentUpdateRequest;
import ao.gov.embaixada.gpj.dto.IncidentUpdateResponse;
import ao.gov.embaixada.gpj.entity.IncidentUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentUpdateMapper {

    IncidentUpdateResponse toResponse(IncidentUpdate entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "incident", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    IncidentUpdate toEntity(IncidentUpdateRequest request);
}
