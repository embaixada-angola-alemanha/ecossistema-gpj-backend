package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.DeploymentCreateRequest;
import ao.gov.embaixada.gpj.dto.DeploymentResponse;
import ao.gov.embaixada.gpj.entity.Deployment;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.DeploymentEnvironment;
import ao.gov.embaixada.gpj.enums.DeploymentStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.DeploymentMapper;
import ao.gov.embaixada.gpj.repository.DeploymentRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);

    private final DeploymentRepository deploymentRepository;
    private final MonitoredServiceRepository serviceRepository;
    private final DeploymentMapper mapper;

    public DeploymentService(DeploymentRepository deploymentRepository,
                             MonitoredServiceRepository serviceRepository,
                             DeploymentMapper mapper) {
        this.deploymentRepository = deploymentRepository;
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
    }

    @Transactional
    public DeploymentResponse create(DeploymentCreateRequest request, String deployedBy) {
        MonitoredService service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("MonitoredService", request.serviceId()));

        Deployment deployment = mapper.toEntity(request);
        deployment.setService(service);
        deployment.setDeployedBy(deployedBy);
        deployment.setDeployedAt(Instant.now());
        deployment.setStatus(DeploymentStatus.SUCCESS);

        Deployment saved = deploymentRepository.save(deployment);
        log.info("Created deployment: id={}, service={}, version={}, env={}",
                saved.getId(), service.getName(), saved.getVersionTag(), saved.getEnvironment());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DeploymentResponse findById(UUID id) {
        Deployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment", id));
        return mapper.toResponse(deployment);
    }

    @Transactional(readOnly = true)
    public Page<DeploymentResponse> findAll(UUID serviceId, DeploymentEnvironment env, Pageable pageable) {
        if (serviceId != null) {
            return deploymentRepository.findByServiceId(serviceId, pageable)
                    .map(mapper::toResponse);
        }
        if (env != null) {
            return deploymentRepository.findByEnvironment(env, pageable)
                    .map(mapper::toResponse);
        }
        return deploymentRepository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DeploymentResponse> findRecent() {
        return deploymentRepository.findTop10ByOrderByDeployedAtDesc().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
