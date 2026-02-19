package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.commons.integration.event.IntegrationEvent;
import ao.gov.embaixada.gpj.dto.SystemEventResponse;
import ao.gov.embaixada.gpj.entity.SystemEvent;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.SystemEventMapper;
import ao.gov.embaixada.gpj.repository.SystemEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SystemEventServiceTest {

    @Mock
    private SystemEventRepository repository;

    @Mock
    private SystemEventMapper mapper;

    @InjectMocks
    private SystemEventService systemEventService;

    private SystemEvent createEntity(UUID id) {
        SystemEvent e = new SystemEvent();
        e.setId(id);
        e.setEventId(UUID.randomUUID());
        e.setSource("SGC");
        e.setEventType("CIDADAO_CREATED");
        e.setEntityType("Cidadao");
        e.setEntityId("entity-1");
        e.setTimestamp(Instant.now());
        e.setReceivedAt(Instant.now());
        e.setProcessed(true);
        return e;
    }

    private SystemEventResponse createResponse(UUID id) {
        return new SystemEventResponse(
                id, UUID.randomUUID(), "SGC", "CIDADAO_CREATED",
                "Cidadao", "entity-1", Instant.now(), Instant.now());
    }

    @Test
    void persistEvent_newEvent_shouldSave() {
        UUID eventId = UUID.randomUUID();
        IntegrationEvent event = new IntegrationEvent(
                eventId, "SGC", "CIDADAO_CREATED", "entity-1", "Cidadao",
                Map.of("name", "John"), Instant.now(), null);

        when(repository.existsByEventId(eventId)).thenReturn(false);
        when(repository.save(any(SystemEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        systemEventService.persistEvent(event);

        verify(repository).save(argThat(se -> {
            return se.getEventId().equals(eventId)
                    && "SGC".equals(se.getSource())
                    && "CIDADAO_CREATED".equals(se.getEventType())
                    && se.isProcessed();
        }));
    }

    @Test
    void persistEvent_duplicateEvent_shouldSkip() {
        UUID eventId = UUID.randomUUID();
        IntegrationEvent event = new IntegrationEvent(
                eventId, "SGC", "CIDADAO_CREATED", "entity-1", "Cidadao",
                Map.of(), Instant.now(), null);

        when(repository.existsByEventId(eventId)).thenReturn(true);

        systemEventService.persistEvent(event);

        verify(repository, never()).save(any());
    }

    @Test
    void persistEvent_nullPayload_shouldHandleGracefully() {
        UUID eventId = UUID.randomUUID();
        IntegrationEvent event = new IntegrationEvent(
                eventId, "SGC", "CIDADAO_CREATED", "entity-1", "Cidadao",
                null, Instant.now(), null);

        when(repository.existsByEventId(eventId)).thenReturn(false);
        when(repository.save(any(SystemEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        systemEventService.persistEvent(event);

        verify(repository).save(argThat(se -> se.getPayload() == null));
    }

    @Test
    void persistEvent_emptyPayload_shouldHandleGracefully() {
        UUID eventId = UUID.randomUUID();
        IntegrationEvent event = new IntegrationEvent(
                eventId, "SGC", "CIDADAO_CREATED", "entity-1", "Cidadao",
                Map.of(), Instant.now(), null);

        when(repository.existsByEventId(eventId)).thenReturn(false);
        when(repository.save(any(SystemEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        systemEventService.persistEvent(event);

        verify(repository).save(argThat(se -> se.getPayload() == null));
    }

    @Test
    void findAll_bySource_shouldFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        SystemEvent entity = createEntity(UUID.randomUUID());
        Page<SystemEvent> page = new PageImpl<>(List.of(entity));

        when(repository.findBySource("SGC", pageable)).thenReturn(page);
        when(mapper.toResponse(any())).thenReturn(createResponse(entity.getId()));

        Page<SystemEventResponse> result = systemEventService.findAll("SGC", null, pageable);

        assertEquals(1, result.getContent().size());
        verify(repository).findBySource("SGC", pageable);
    }

    @Test
    void findAll_byEventType_shouldFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        SystemEvent entity = createEntity(UUID.randomUUID());
        Page<SystemEvent> page = new PageImpl<>(List.of(entity));

        when(repository.findByEventType("CIDADAO_CREATED", pageable)).thenReturn(page);
        when(mapper.toResponse(any())).thenReturn(createResponse(entity.getId()));

        Page<SystemEventResponse> result = systemEventService.findAll(null, "CIDADAO_CREATED", pageable);

        assertEquals(1, result.getContent().size());
        verify(repository).findByEventType("CIDADAO_CREATED", pageable);
    }

    @Test
    void findAll_noFilter_shouldReturnAllOrderedByTimestamp() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SystemEvent> page = new PageImpl<>(List.of());

        when(repository.findAllByOrderByTimestampDesc(pageable)).thenReturn(page);

        Page<SystemEventResponse> result = systemEventService.findAll(null, null, pageable);

        assertEquals(0, result.getContent().size());
        verify(repository).findAllByOrderByTimestampDesc(pageable);
    }

    @Test
    void findAll_blankSource_shouldTreatAsNoFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SystemEvent> page = new PageImpl<>(List.of());

        when(repository.findAllByOrderByTimestampDesc(pageable)).thenReturn(page);

        systemEventService.findAll("  ", null, pageable);

        verify(repository).findAllByOrderByTimestampDesc(pageable);
    }

    @Test
    void findById_shouldReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        SystemEvent entity = createEntity(id);
        SystemEventResponse response = createResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        SystemEventResponse result = systemEventService.findById(id);

        assertEquals(id, result.id());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> systemEventService.findById(id));
    }

    @Test
    void countToday_shouldDelegateToRepository() {
        when(repository.countSince(any())).thenReturn(42L);

        long count = systemEventService.countToday();

        assertEquals(42L, count);
        verify(repository).countSince(any());
    }

    @Test
    void countBySourceSince_shouldDelegateToRepository() {
        Instant since = Instant.now().minusSeconds(86400);
        List<Object[]> results = List.of(new Object[]{"SGC", 10L}, new Object[]{"SI", 5L});

        when(repository.countBySourceSince(since)).thenReturn(results);

        List<Object[]> result = systemEventService.countBySourceSince(since);

        assertEquals(2, result.size());
    }
}
