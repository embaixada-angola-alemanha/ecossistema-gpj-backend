package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.MonitoredServiceCreateRequest;
import ao.gov.embaixada.gpj.dto.MonitoredServiceResponse;
import ao.gov.embaixada.gpj.dto.MonitoredServiceUpdateRequest;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.MonitoredServiceMapper;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MonitoredServiceService {

    private static final Logger log = LoggerFactory.getLogger(MonitoredServiceService.class);

    private final MonitoredServiceRepository repository;
    private final MonitoredServiceMapper mapper;

    public MonitoredServiceService(MonitoredServiceRepository repository,
                                   MonitoredServiceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MonitoredServiceResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonitoredServiceResponse findById(UUID id) {
        MonitoredService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonitoredService", id));
        return mapper.toResponse(service);
    }

    @Transactional
    public MonitoredServiceResponse create(MonitoredServiceCreateRequest request) {
        MonitoredService entity = mapper.toEntity(request);
        entity.setStatus(ServiceStatus.UNKNOWN);
        entity.setConsecutiveFailures(0);
        MonitoredService saved = repository.save(entity);
        log.info("Created monitored service: name={}, id={}", saved.getName(), saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional
    public MonitoredServiceResponse update(UUID id, MonitoredServiceUpdateRequest request) {
        MonitoredService entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonitoredService", id));

        if (request.displayName() != null) {
            entity.setDisplayName(request.displayName());
        }
        if (request.healthUrl() != null) {
            entity.setHealthUrl(request.healthUrl());
        }
        if (request.metadata() != null) {
            entity.setMetadata(request.metadata());
        }

        MonitoredService saved = repository.save(entity);
        log.info("Updated monitored service: id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("MonitoredService", id);
        }
        repository.deleteById(id);
        log.info("Deleted monitored service: id={}", id);
    }
}
