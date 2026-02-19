package ao.gov.embaixada.gpj.mapper;

import ao.gov.embaixada.gpj.dto.SystemEventResponse;
import ao.gov.embaixada.gpj.entity.SystemEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemEventMapper {

    SystemEventResponse toResponse(SystemEvent entity);
}
