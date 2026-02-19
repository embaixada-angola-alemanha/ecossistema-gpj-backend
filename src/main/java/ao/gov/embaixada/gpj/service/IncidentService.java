package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.IncidentCreateRequest;
import ao.gov.embaixada.gpj.dto.IncidentResolveRequest;
import ao.gov.embaixada.gpj.dto.IncidentResponse;
import ao.gov.embaixada.gpj.dto.IncidentUpdateRequest;
import ao.gov.embaixada.gpj.entity.Incident;
import ao.gov.embaixada.gpj.entity.IncidentUpdate;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.IncidentSeverity;
import ao.gov.embaixada.gpj.enums.IncidentStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.integration.GopEventPublisher;
import ao.gov.embaixada.gpj.mapper.IncidentMapper;
import ao.gov.embaixada.gpj.mapper.IncidentUpdateMapper;
import ao.gov.embaixada.gpj.repository.IncidentRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import ao.gov.embaixada.gpj.statemachine.IncidentStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;
    private final MonitoredServiceRepository serviceRepository;
    private final IncidentMapper incidentMapper;
    private final IncidentUpdateMapper updateMapper;
    private final GopEventPublisher eventPublisher;

    public IncidentService(IncidentRepository incidentRepository,
                           MonitoredServiceRepository serviceRepository,
                           IncidentMapper incidentMapper,
                           IncidentUpdateMapper updateMapper,
                           GopEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.serviceRepository = serviceRepository;
        this.incidentMapper = incidentMapper;
        this.updateMapper = updateMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public IncidentResponse create(IncidentCreateRequest request, String reportedBy) {
        Incident incident = incidentMapper.toEntity(request);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedBy(reportedBy);

        if (request.affectedServiceIds() != null && !request.affectedServiceIds().isEmpty()) {
            Set<MonitoredService> services = new HashSet<>(
                    serviceRepository.findAllById(request.affectedServiceIds()));
            incident.setAffectedServices(services);
        }

        Incident saved = incidentRepository.save(incident);
        log.info("Created incident: id={}, title={}, severity={}", saved.getId(), saved.getTitle(), saved.getSeverity());

        eventPublisher.incidentCreated(saved.getId(), saved.getTitle(), saved.getSeverity().name());

        return incidentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncidentResponse findById(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
        return incidentMapper.toResponse(incident);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> findAll(IncidentStatus status, IncidentSeverity severity, Pageable pageable) {
        if (status != null) {
            return incidentRepository.findByStatus(status, pageable)
                    .map(incidentMapper::toResponse);
        }
        if (severity != null) {
            return incidentRepository.findBySeverity(severity, pageable)
                    .map(incidentMapper::toResponse);
        }
        return incidentRepository.findAll(pageable)
                .map(incidentMapper::toResponse);
    }

    @Transactional
    public IncidentResponse updateStatus(UUID id, IncidentStatus newStatus) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        IncidentStateMachine.validateTransition(incident.getStatus(), newStatus);
        incident.setStatus(newStatus);

        Incident saved = incidentRepository.save(incident);
        log.info("Updated incident status: id={}, newStatus={}", id, newStatus);
        return incidentMapper.toResponse(saved);
    }

    @Transactional
    public IncidentResponse addUpdate(UUID id, IncidentUpdateRequest request, String author) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        IncidentUpdate update = updateMapper.toEntity(request);
        update.setIncident(incident);
        update.setAuthor(author);
        incident.getUpdates().add(update);

        Incident saved = incidentRepository.save(incident);
        log.info("Added update to incident: id={}, author={}", id, author);
        return incidentMapper.toResponse(saved);
    }

    @Transactional
    public IncidentResponse resolve(UUID id, IncidentResolveRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        IncidentStateMachine.validateTransition(incident.getStatus(), IncidentStatus.RESOLVED);
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(Instant.now());
        incident.setRootCause(request.rootCause());
        incident.setResolution(request.resolution());

        Incident saved = incidentRepository.save(incident);
        log.info("Resolved incident: id={}", id);

        eventPublisher.incidentResolved(saved.getId(), saved.getTitle());

        return incidentMapper.toResponse(saved);
    }

    @Transactional
    public void close(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        IncidentStateMachine.validateTransition(incident.getStatus(), IncidentStatus.CLOSED);
        incident.setStatus(IncidentStatus.CLOSED);

        incidentRepository.save(incident);
        log.info("Closed incident: id={}", id);
    }

    @Transactional
    public void autoCreateFromHealthFailure(MonitoredService service) {
        // Check that no open incident already exists for this service
        List<IncidentStatus> activeStatuses = List.of(
                IncidentStatus.OPEN,
                IncidentStatus.INVESTIGATING,
                IncidentStatus.IDENTIFIED,
                IncidentStatus.MONITORING
        );
        List<Incident> existing = incidentRepository.findByStatusInAndAffectedServicesContaining(
                activeStatuses, service);

        if (!existing.isEmpty()) {
            log.debug("Active incident already exists for service {}, skipping auto-create",
                    service.getDisplayName());
            return;
        }

        Incident incident = new Incident();
        incident.setTitle("Service DOWN: " + service.getDisplayName());
        incident.setDescription("Automated incident: service " + service.getDisplayName()
                + " has failed " + service.getConsecutiveFailures() + " consecutive health checks.");
        incident.setSeverity(IncidentSeverity.P2);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedBy("SYSTEM");
        incident.setAffectedServices(Set.of(service));

        Incident saved = incidentRepository.save(incident);
        log.warn("Auto-created incident for service DOWN: {} (id={})",
                service.getDisplayName(), saved.getId());

        eventPublisher.serviceDown(service.getDisplayName());
        eventPublisher.incidentCreated(saved.getId(), saved.getTitle(), saved.getSeverity().name());
    }
}
