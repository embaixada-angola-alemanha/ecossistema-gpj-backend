package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.MonitoredServiceCreateRequest;
import ao.gov.embaixada.gpj.dto.MonitoredServiceResponse;
import ao.gov.embaixada.gpj.dto.MonitoredServiceUpdateRequest;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.enums.ServiceType;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.MonitoredServiceMapper;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class MonitoredServiceServiceTest {

    @Mock
    private MonitoredServiceRepository repository;

    @Mock
    private MonitoredServiceMapper mapper;

    @InjectMocks
    private MonitoredServiceService service;

    private MonitoredService createEntity(UUID id, String name) {
        MonitoredService entity = new MonitoredService();
        entity.setId(id);
        entity.setName(name);
        entity.setDisplayName(name + " Display");
        entity.setType(ServiceType.BACKEND);
        entity.setStatus(ServiceStatus.UP);
        entity.setConsecutiveFailures(0);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private MonitoredServiceResponse createResponse(UUID id, String name) {
        return new MonitoredServiceResponse(
                id, name, name + " Display", "BACKEND", "UP",
                null, null, 0, Instant.now());
    }

    @Test
    void findAll_shouldReturnMappedList() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        MonitoredService e1 = createEntity(id1, "sgc-backend");
        MonitoredService e2 = createEntity(id2, "si-backend");
        MonitoredServiceResponse r1 = createResponse(id1, "sgc-backend");
        MonitoredServiceResponse r2 = createResponse(id2, "si-backend");

        when(repository.findAll()).thenReturn(List.of(e1, e2));
        when(mapper.toResponse(e1)).thenReturn(r1);
        when(mapper.toResponse(e2)).thenReturn(r2);

        List<MonitoredServiceResponse> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("sgc-backend", result.get(0).name());
        verify(repository).findAll();
    }

    @Test
    void findById_shouldReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        MonitoredService entity = createEntity(id, "sgc-backend");
        MonitoredServiceResponse response = createResponse(id, "sgc-backend");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        MonitoredServiceResponse result = service.findById(id);

        assertEquals(id, result.id());
        assertEquals("sgc-backend", result.name());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void create_shouldSetDefaultStatusAndSave() {
        MonitoredServiceCreateRequest request = new MonitoredServiceCreateRequest(
                "sgc-backend", "SGC Backend", "BACKEND", "http://localhost:8081/actuator/health", null);

        MonitoredService newEntity = new MonitoredService();
        MonitoredService savedEntity = createEntity(UUID.randomUUID(), "sgc-backend");
        savedEntity.setStatus(ServiceStatus.UNKNOWN);
        MonitoredServiceResponse response = createResponse(savedEntity.getId(), "sgc-backend");

        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(repository.save(any(MonitoredService.class))).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        MonitoredServiceResponse result = service.create(request);

        assertNotNull(result);
        verify(repository).save(any(MonitoredService.class));
        assertEquals(ServiceStatus.UNKNOWN, newEntity.getStatus());
        assertEquals(0, newEntity.getConsecutiveFailures());
    }

    @Test
    void update_shouldPatchFields() {
        UUID id = UUID.randomUUID();
        MonitoredService entity = createEntity(id, "sgc-backend");
        MonitoredServiceUpdateRequest request = new MonitoredServiceUpdateRequest(
                "SGC Backend Updated", "http://new-url/health", null);
        MonitoredServiceResponse response = new MonitoredServiceResponse(
                id, "sgc-backend", "SGC Backend Updated", "BACKEND", "UP",
                null, null, 0, Instant.now());

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        MonitoredServiceResponse result = service.update(id, request);

        assertEquals("SGC Backend Updated", result.displayName());
        assertEquals("SGC Backend Updated", entity.getDisplayName());
        assertEquals("http://new-url/health", entity.getHealthUrl());
    }

    @Test
    void update_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(id, new MonitoredServiceUpdateRequest("name", null, null)));
    }

    @Test
    void update_nullFields_shouldNotOverwrite() {
        UUID id = UUID.randomUUID();
        MonitoredService entity = createEntity(id, "sgc-backend");
        entity.setDisplayName("Original");
        entity.setHealthUrl("http://original/health");
        entity.setMetadata("{\"key\":\"value\"}");

        MonitoredServiceUpdateRequest request = new MonitoredServiceUpdateRequest(null, null, null);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(createResponse(id, "sgc-backend"));

        service.update(id, request);

        assertEquals("Original", entity.getDisplayName());
        assertEquals("http://original/health", entity.getHealthUrl());
        assertEquals("{\"key\":\"value\"}", entity.getMetadata());
    }

    @Test
    void delete_shouldCallRepository() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void delete_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(repository, never()).deleteById(any());
    }
}
