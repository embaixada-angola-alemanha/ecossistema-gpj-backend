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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class DeploymentServiceTest {

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private DeploymentMapper mapper;

    @InjectMocks
    private DeploymentService deploymentService;

    private Deployment createDeployment(UUID id, UUID serviceId) {
        Deployment d = new Deployment();
        d.setId(id);
        MonitoredService svc = new MonitoredService();
        svc.setId(serviceId);
        svc.setName("sgc-backend");
        svc.setDisplayName("SGC Backend");
        d.setService(svc);
        d.setVersionTag("1.0.0");
        d.setEnvironment(DeploymentEnvironment.PRODUCTION);
        d.setStatus(DeploymentStatus.SUCCESS);
        d.setDeployedAt(Instant.now());
        d.setDeployedBy("admin");
        return d;
    }

    private DeploymentResponse createResponse(UUID id, UUID serviceId) {
        return new DeploymentResponse(
                id, serviceId, "SGC Backend", "1.0.0", "abc123",
                "PRODUCTION", "admin", Instant.now(), "SUCCESS", null, Instant.now());
    }

    @Test
    void create_shouldResolveServiceAndSetDefaults() {
        UUID serviceId = UUID.randomUUID();
        UUID deployId = UUID.randomUUID();
        DeploymentCreateRequest request = new DeploymentCreateRequest(
                serviceId, "1.0.0", "abc123", "PRODUCTION", null);

        MonitoredService svc = new MonitoredService();
        svc.setId(serviceId);
        svc.setName("sgc-backend");

        Deployment newEntity = new Deployment();
        Deployment saved = createDeployment(deployId, serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(svc));
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(deploymentRepository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(createResponse(deployId, serviceId));

        DeploymentResponse result = deploymentService.create(request, "admin");

        assertNotNull(result);
        assertEquals(svc, newEntity.getService());
        assertEquals("admin", newEntity.getDeployedBy());
        assertNotNull(newEntity.getDeployedAt());
        assertEquals(DeploymentStatus.SUCCESS, newEntity.getStatus());
    }

    @Test
    void create_serviceNotFound_shouldThrow() {
        UUID serviceId = UUID.randomUUID();
        DeploymentCreateRequest request = new DeploymentCreateRequest(
                serviceId, "1.0.0", null, "STAGING", null);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deploymentService.create(request, "admin"));
    }

    @Test
    void findById_shouldReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        Deployment deployment = createDeployment(id, UUID.randomUUID());
        DeploymentResponse response = createResponse(id, deployment.getService().getId());

        when(deploymentRepository.findById(id)).thenReturn(Optional.of(deployment));
        when(mapper.toResponse(deployment)).thenReturn(response);

        DeploymentResponse result = deploymentService.findById(id);

        assertEquals(id, result.id());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(deploymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deploymentService.findById(id));
    }

    @Test
    void findAll_byServiceId_shouldFilter() {
        UUID serviceId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Deployment d = createDeployment(UUID.randomUUID(), serviceId);
        Page<Deployment> page = new PageImpl<>(List.of(d));

        when(deploymentRepository.findByServiceId(serviceId, pageable)).thenReturn(page);
        when(mapper.toResponse(any())).thenReturn(createResponse(d.getId(), serviceId));

        Page<DeploymentResponse> result = deploymentService.findAll(serviceId, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(deploymentRepository).findByServiceId(serviceId, pageable);
    }

    @Test
    void findAll_byEnvironment_shouldFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        Deployment d = createDeployment(UUID.randomUUID(), UUID.randomUUID());
        Page<Deployment> page = new PageImpl<>(List.of(d));

        when(deploymentRepository.findByEnvironment(DeploymentEnvironment.PRODUCTION, pageable)).thenReturn(page);
        when(mapper.toResponse(any())).thenReturn(createResponse(d.getId(), d.getService().getId()));

        Page<DeploymentResponse> result = deploymentService.findAll(null, DeploymentEnvironment.PRODUCTION, pageable);

        assertEquals(1, result.getContent().size());
        verify(deploymentRepository).findByEnvironment(DeploymentEnvironment.PRODUCTION, pageable);
    }

    @Test
    void findAll_noFilters_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Deployment> page = new PageImpl<>(List.of());

        when(deploymentRepository.findAll(pageable)).thenReturn(page);

        Page<DeploymentResponse> result = deploymentService.findAll(null, null, pageable);

        assertEquals(0, result.getContent().size());
        verify(deploymentRepository).findAll(pageable);
    }

    @Test
    void findRecent_shouldReturnTop10() {
        List<Deployment> deployments = List.of(
                createDeployment(UUID.randomUUID(), UUID.randomUUID()));

        when(deploymentRepository.findTop10ByOrderByDeployedAtDesc()).thenReturn(deployments);
        when(mapper.toResponse(any())).thenReturn(
                createResponse(deployments.get(0).getId(), deployments.get(0).getService().getId()));

        List<DeploymentResponse> result = deploymentService.findRecent();

        assertEquals(1, result.size());
        verify(deploymentRepository).findTop10ByOrderByDeployedAtDesc();
    }
}
