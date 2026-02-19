package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.*;
import ao.gov.embaixada.gpj.entity.Incident;
import ao.gov.embaixada.gpj.entity.IncidentUpdate;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.IncidentSeverity;
import ao.gov.embaixada.gpj.enums.IncidentStatus;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.integration.GopEventPublisher;
import ao.gov.embaixada.gpj.mapper.IncidentMapper;
import ao.gov.embaixada.gpj.mapper.IncidentUpdateMapper;
import ao.gov.embaixada.gpj.repository.IncidentRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
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
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private IncidentMapper incidentMapper;

    @Mock
    private IncidentUpdateMapper updateMapper;

    @Mock
    private GopEventPublisher eventPublisher;

    @InjectMocks
    private IncidentService incidentService;

    private Incident createIncident(UUID id, IncidentStatus status, IncidentSeverity severity) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setTitle("Test Incident");
        incident.setDescription("Test description");
        incident.setSeverity(severity);
        incident.setStatus(status);
        incident.setReportedBy("admin");
        incident.setCreatedAt(Instant.now());
        return incident;
    }

    private IncidentResponse createResponse(UUID id, String status) {
        return new IncidentResponse(
                id, "Test Incident", "Test description", "P2", status,
                List.of(), "admin", null, null, null, null,
                List.of(), Instant.now(), Instant.now());
    }

    @Test
    void create_shouldSetOpenStatusAndPublishEvent() {
        UUID id = UUID.randomUUID();
        IncidentCreateRequest request = new IncidentCreateRequest(
                "Service Down", "SGC is down", "P2", null, null);

        Incident newEntity = new Incident();
        Incident saved = createIncident(id, IncidentStatus.OPEN, IncidentSeverity.P2);
        IncidentResponse response = createResponse(id, "OPEN");

        when(incidentMapper.toEntity(request)).thenReturn(newEntity);
        when(incidentRepository.save(any(Incident.class))).thenReturn(saved);
        when(incidentMapper.toResponse(saved)).thenReturn(response);

        IncidentResponse result = incidentService.create(request, "admin");

        assertEquals("OPEN", result.status());
        verify(incidentRepository).save(any(Incident.class));
        verify(eventPublisher).incidentCreated(id, "Test Incident", "P2");
        assertEquals(IncidentStatus.OPEN, newEntity.getStatus());
        assertEquals("admin", newEntity.getReportedBy());
    }

    @Test
    void create_withAffectedServices_shouldResolveServices() {
        UUID serviceId = UUID.randomUUID();
        IncidentCreateRequest request = new IncidentCreateRequest(
                "Service Down", "desc", "P1", List.of(serviceId), null);

        MonitoredService svc = new MonitoredService();
        svc.setId(serviceId);

        Incident newEntity = new Incident();
        Incident saved = createIncident(UUID.randomUUID(), IncidentStatus.OPEN, IncidentSeverity.P1);

        when(incidentMapper.toEntity(request)).thenReturn(newEntity);
        when(serviceRepository.findAllById(List.of(serviceId))).thenReturn(List.of(svc));
        when(incidentRepository.save(any())).thenReturn(saved);
        when(incidentMapper.toResponse(any())).thenReturn(createResponse(saved.getId(), "OPEN"));

        incidentService.create(request, "admin");

        verify(serviceRepository).findAllById(List.of(serviceId));
        assertNotNull(newEntity.getAffectedServices());
    }

    @Test
    void findById_shouldReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        Incident incident = createIncident(id, IncidentStatus.OPEN, IncidentSeverity.P2);
        IncidentResponse response = createResponse(id, "OPEN");

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));
        when(incidentMapper.toResponse(incident)).thenReturn(response);

        IncidentResponse result = incidentService.findById(id);

        assertEquals(id, result.id());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> incidentService.findById(id));
    }

    @Test
    void findAll_noFilters_shouldReturnAllPaged() {
        Pageable pageable = PageRequest.of(0, 20);
        Incident incident = createIncident(UUID.randomUUID(), IncidentStatus.OPEN, IncidentSeverity.P2);
        Page<Incident> page = new PageImpl<>(List.of(incident));

        when(incidentRepository.findAll(pageable)).thenReturn(page);
        when(incidentMapper.toResponse(any())).thenReturn(createResponse(incident.getId(), "OPEN"));

        Page<IncidentResponse> result = incidentService.findAll(null, null, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAll_byStatus_shouldFilterByStatus() {
        Pageable pageable = PageRequest.of(0, 20);
        Incident incident = createIncident(UUID.randomUUID(), IncidentStatus.OPEN, IncidentSeverity.P2);
        Page<Incident> page = new PageImpl<>(List.of(incident));

        when(incidentRepository.findByStatus(IncidentStatus.OPEN, pageable)).thenReturn(page);
        when(incidentMapper.toResponse(any())).thenReturn(createResponse(incident.getId(), "OPEN"));

        Page<IncidentResponse> result = incidentService.findAll(IncidentStatus.OPEN, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(incidentRepository).findByStatus(IncidentStatus.OPEN, pageable);
    }

    @Test
    void findAll_bySeverity_shouldFilterBySeverity() {
        Pageable pageable = PageRequest.of(0, 20);
        Incident incident = createIncident(UUID.randomUUID(), IncidentStatus.OPEN, IncidentSeverity.P1);
        Page<Incident> page = new PageImpl<>(List.of(incident));

        when(incidentRepository.findBySeverity(IncidentSeverity.P1, pageable)).thenReturn(page);
        when(incidentMapper.toResponse(any())).thenReturn(createResponse(incident.getId(), "OPEN"));

        Page<IncidentResponse> result = incidentService.findAll(null, IncidentSeverity.P1, pageable);

        assertEquals(1, result.getContent().size());
        verify(incidentRepository).findBySeverity(IncidentSeverity.P1, pageable);
    }

    @Test
    void updateStatus_validTransition_shouldUpdate() {
        UUID id = UUID.randomUUID();
        Incident incident = createIncident(id, IncidentStatus.OPEN, IncidentSeverity.P2);

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentMapper.toResponse(any())).thenReturn(createResponse(id, "INVESTIGATING"));

        IncidentResponse result = incidentService.updateStatus(id, IncidentStatus.INVESTIGATING);

        assertEquals(IncidentStatus.INVESTIGATING, incident.getStatus());
        verify(incidentRepository).save(incident);
    }

    @Test
    void updateStatus_invalidTransition_shouldThrow() {
        UUID id = UUID.randomUUID();
        Incident incident = createIncident(id, IncidentStatus.OPEN, IncidentSeverity.P2);

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));

        assertThrows(InvalidStateTransitionException.class,
                () -> incidentService.updateStatus(id, IncidentStatus.RESOLVED));
    }

    @Test
    void addUpdate_shouldPersistUpdateOnIncident() {
        UUID id = UUID.randomUUID();
        Incident incident = createIncident(id, IncidentStatus.INVESTIGATING, IncidentSeverity.P2);
        IncidentUpdateRequest request = new IncidentUpdateRequest("Investigating root cause");
        IncidentUpdate update = new IncidentUpdate();
        update.setMessage("Investigating root cause");

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));
        when(updateMapper.toEntity(request)).thenReturn(update);
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentMapper.toResponse(any())).thenReturn(createResponse(id, "INVESTIGATING"));

        incidentService.addUpdate(id, request, "operator");

        assertEquals(incident, update.getIncident());
        assertEquals("operator", update.getAuthor());
        assertTrue(incident.getUpdates().contains(update));
    }

    @Test
    void resolve_validTransition_shouldSetResolvedFields() {
        UUID id = UUID.randomUUID();
        Incident incident = createIncident(id, IncidentStatus.INVESTIGATING, IncidentSeverity.P2);
        IncidentResolveRequest request = new IncidentResolveRequest("DB connection pool exhausted", "Increased pool size");

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentMapper.toResponse(any())).thenReturn(createResponse(id, "RESOLVED"));

        incidentService.resolve(id, request);

        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertNotNull(incident.getResolvedAt());
        assertEquals("DB connection pool exhausted", incident.getRootCause());
        assertEquals("Increased pool size", incident.getResolution());
        verify(eventPublisher).incidentResolved(id, "Test Incident");
    }

    @Test
    void resolve_fromClosedState_shouldThrow() {
        UUID id = UUID.randomUUID();
        Incident incident = createIncident(id, IncidentStatus.CLOSED, IncidentSeverity.P2);

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));

        assertThrows(InvalidStateTransitionException.class,
                () -> incidentService.resolve(id, new IncidentResolveRequest("cause", "fix")));
    }

    @Test
    void close_validTransition_shouldSetClosedStatus() {
        UUID id = UUID.randomUUID();
        Incident incident = createIncident(id, IncidentStatus.RESOLVED, IncidentSeverity.P2);

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenReturn(incident);

        incidentService.close(id);

        assertEquals(IncidentStatus.CLOSED, incident.getStatus());
    }

    @Test
    void close_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> incidentService.close(id));
    }

    @Test
    void autoCreateFromHealthFailure_noExistingIncident_shouldCreate() {
        MonitoredService svc = new MonitoredService();
        svc.setId(UUID.randomUUID());
        svc.setDisplayName("SGC Backend");
        svc.setConsecutiveFailures(5);

        when(incidentRepository.findByStatusInAndAffectedServicesContaining(anyList(), eq(svc)))
                .thenReturn(Collections.emptyList());
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        incidentService.autoCreateFromHealthFailure(svc);

        verify(incidentRepository).save(argThat(incident -> {
            return incident.getTitle().contains("SGC Backend")
                    && incident.getStatus() == IncidentStatus.OPEN
                    && incident.getSeverity() == IncidentSeverity.P2
                    && "SYSTEM".equals(incident.getReportedBy());
        }));
        verify(eventPublisher).serviceDown("SGC Backend");
    }

    @Test
    void autoCreateFromHealthFailure_existingActiveIncident_shouldSkip() {
        MonitoredService svc = new MonitoredService();
        svc.setId(UUID.randomUUID());
        svc.setDisplayName("SGC Backend");

        Incident existing = createIncident(UUID.randomUUID(), IncidentStatus.OPEN, IncidentSeverity.P2);

        when(incidentRepository.findByStatusInAndAffectedServicesContaining(anyList(), eq(svc)))
                .thenReturn(List.of(existing));

        incidentService.autoCreateFromHealthFailure(svc);

        verify(incidentRepository, never()).save(any());
        verify(eventPublisher, never()).serviceDown(any());
    }
}
