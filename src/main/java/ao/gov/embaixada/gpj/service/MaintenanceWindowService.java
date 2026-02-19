package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.MaintenanceCreateRequest;
import ao.gov.embaixada.gpj.dto.MaintenanceResponse;
import ao.gov.embaixada.gpj.dto.MaintenanceUpdateRequest;
import ao.gov.embaixada.gpj.entity.MaintenanceWindow;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.MaintenanceStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.MaintenanceMapper;
import ao.gov.embaixada.gpj.repository.MaintenanceWindowRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import ao.gov.embaixada.gpj.statemachine.MaintenanceStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceWindowService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceWindowService.class);

    private final MaintenanceWindowRepository maintenanceRepository;
    private final MonitoredServiceRepository serviceRepository;
    private final MaintenanceMapper mapper;

    public MaintenanceWindowService(MaintenanceWindowRepository maintenanceRepository,
                                    MonitoredServiceRepository serviceRepository,
                                    MaintenanceMapper mapper) {
        this.maintenanceRepository = maintenanceRepository;
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
    }

    @Transactional
    public MaintenanceResponse create(MaintenanceCreateRequest request, String createdByUser) {
        MaintenanceWindow entity = mapper.toEntity(request);
        entity.setStatus(MaintenanceStatus.SCHEDULED);
        entity.setCreatedByUser(createdByUser);

        if (request.affectedServiceIds() != null && !request.affectedServiceIds().isEmpty()) {
            entity.setAffectedServices(new HashSet<>(
                    serviceRepository.findAllById(request.affectedServiceIds())));
        }

        MaintenanceWindow saved = maintenanceRepository.save(entity);
        log.info("Created maintenance window: id={}, title={}", saved.getId(), saved.getTitle());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse findById(UUID id) {
        MaintenanceWindow entity = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", id));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceResponse> findAll(MaintenanceStatus status, Pageable pageable) {
        if (status != null) {
            List<MaintenanceWindow> filtered = maintenanceRepository.findByStatus(status);
            // Convert to Page manually since repository returns List
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<MaintenanceWindow> pageContent = start < filtered.size()
                    ? filtered.subList(start, end)
                    : List.of();
            Page<MaintenanceWindow> page = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, filtered.size());
            return page.map(mapper::toResponse);
        }
        return maintenanceRepository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public MaintenanceResponse update(UUID id, MaintenanceUpdateRequest request) {
        MaintenanceWindow entity = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", id));

        if (request.title() != null) {
            entity.setTitle(request.title());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.scheduledStart() != null) {
            entity.setScheduledStart(request.scheduledStart());
        }
        if (request.scheduledEnd() != null) {
            entity.setScheduledEnd(request.scheduledEnd());
        }
        if (request.affectedServiceIds() != null) {
            entity.setAffectedServices(new HashSet<>(
                    serviceRepository.findAllById(request.affectedServiceIds())));
        }

        MaintenanceWindow saved = maintenanceRepository.save(entity);
        log.info("Updated maintenance window: id={}", id);
        return mapper.toResponse(saved);
    }

    @Transactional
    public MaintenanceResponse start(UUID id) {
        MaintenanceWindow entity = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", id));

        MaintenanceStateMachine.validateTransition(entity.getStatus(), MaintenanceStatus.IN_PROGRESS);
        entity.setStatus(MaintenanceStatus.IN_PROGRESS);
        entity.setActualStart(Instant.now());

        MaintenanceWindow saved = maintenanceRepository.save(entity);
        log.info("Started maintenance window: id={}", id);
        return mapper.toResponse(saved);
    }

    @Transactional
    public MaintenanceResponse complete(UUID id) {
        MaintenanceWindow entity = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", id));

        MaintenanceStateMachine.validateTransition(entity.getStatus(), MaintenanceStatus.COMPLETED);
        entity.setStatus(MaintenanceStatus.COMPLETED);
        entity.setActualEnd(Instant.now());

        MaintenanceWindow saved = maintenanceRepository.save(entity);
        log.info("Completed maintenance window: id={}", id);
        return mapper.toResponse(saved);
    }

    @Transactional
    public MaintenanceResponse cancel(UUID id) {
        MaintenanceWindow entity = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", id));

        MaintenanceStateMachine.validateTransition(entity.getStatus(), MaintenanceStatus.CANCELLED);
        entity.setStatus(MaintenanceStatus.CANCELLED);

        MaintenanceWindow saved = maintenanceRepository.save(entity);
        log.info("Cancelled maintenance window: id={}", id);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> findUpcoming() {
        return maintenanceRepository.findByStatus(MaintenanceStatus.SCHEDULED).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
