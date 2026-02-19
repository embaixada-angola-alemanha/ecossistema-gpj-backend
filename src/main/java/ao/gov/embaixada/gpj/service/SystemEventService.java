package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.commons.integration.event.IntegrationEvent;
import ao.gov.embaixada.gpj.dto.SystemEventResponse;
import ao.gov.embaixada.gpj.entity.SystemEvent;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.SystemEventMapper;
import ao.gov.embaixada.gpj.repository.SystemEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class SystemEventService {

    private static final Logger log = LoggerFactory.getLogger(SystemEventService.class);

    private final SystemEventRepository repository;
    private final SystemEventMapper mapper;

    public SystemEventService(SystemEventRepository repository,
                              SystemEventMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public void persistEvent(IntegrationEvent event) {
        if (repository.existsByEventId(event.eventId())) {
            log.debug("Duplicate event detected, skipping: eventId={}", event.eventId());
            return;
        }

        SystemEvent entity = new SystemEvent();
        entity.setEventId(event.eventId());
        entity.setSource(event.source());
        entity.setEventType(event.eventType());
        entity.setEntityType(event.entityType());
        entity.setEntityId(event.entityId());
        entity.setTimestamp(event.timestamp());
        entity.setReceivedAt(Instant.now());
        entity.setProcessed(true);

        if (event.payload() != null && !event.payload().isEmpty()) {
            try {
                entity.setPayload(new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(event.payload()));
            } catch (Exception ex) {
                log.warn("Failed to serialize event payload for eventId={}: {}", event.eventId(), ex.getMessage());
                entity.setPayload("{}");
            }
        }

        repository.save(entity);
        log.debug("Persisted system event: eventId={}, source={}, type={}",
                event.eventId(), event.source(), event.eventType());
    }

    @Transactional(readOnly = true)
    public Page<SystemEventResponse> findAll(String source, String eventType, Pageable pageable) {
        if (source != null && !source.isBlank()) {
            return repository.findBySource(source, pageable)
                    .map(mapper::toResponse);
        }
        if (eventType != null && !eventType.isBlank()) {
            return repository.findByEventType(eventType, pageable)
                    .map(mapper::toResponse);
        }
        return repository.findAllByOrderByTimestampDesc(pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SystemEventResponse findById(UUID id) {
        SystemEvent event = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemEvent", id));
        return mapper.toResponse(event);
    }

    @Transactional(readOnly = true)
    public long countToday() {
        Instant startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        return repository.countSince(startOfToday);
    }

    @Transactional(readOnly = true)
    public List<Object[]> countBySourceSince(Instant since) {
        return repository.countBySourceSince(since);
    }
}
