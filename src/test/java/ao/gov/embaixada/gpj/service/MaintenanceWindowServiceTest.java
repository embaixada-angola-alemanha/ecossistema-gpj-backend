package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.MaintenanceCreateRequest;
import ao.gov.embaixada.gpj.dto.MaintenanceResponse;
import ao.gov.embaixada.gpj.dto.MaintenanceUpdateRequest;
import ao.gov.embaixada.gpj.entity.MaintenanceWindow;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.MaintenanceStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.MaintenanceMapper;
import ao.gov.embaixada.gpj.repository.MaintenanceWindowRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MaintenanceWindowServiceTest {

    @Mock
    private MaintenanceWindowRepository maintenanceRepository;

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private MaintenanceMapper mapper;

    @InjectMocks
    private MaintenanceWindowService maintenanceWindowService;

    private MaintenanceWindow createEntity(UUID id, MaintenanceStatus status) {
        MaintenanceWindow mw = new MaintenanceWindow();
        mw.setId(id);
        mw.setTitle("DB Maintenance");
        mw.setDescription("Database upgrade");
        mw.setScheduledStart(Instant.now().plus(1, ChronoUnit.DAYS));
        mw.setScheduledEnd(Instant.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS));
        mw.setStatus(status);
        mw.setCreatedByUser("admin");
        mw.setCreatedAt(Instant.now());
        return mw;
    }

    private MaintenanceResponse createResponse(UUID id, String status) {
        return new MaintenanceResponse(
                id, "DB Maintenance", "Database upgrade",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS),
                null, null, status, List.of(), "admin", Instant.now());
    }

    @Test
    void create_shouldSetScheduledStatusAndSave() {
        UUID id = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                "DB Maintenance", "Upgrade", start, end, null);

        MaintenanceWindow newEntity = new MaintenanceWindow();
        MaintenanceWindow saved = createEntity(id, MaintenanceStatus.SCHEDULED);

        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(maintenanceRepository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(createResponse(id, "SCHEDULED"));

        MaintenanceResponse result = maintenanceWindowService.create(request, "admin");

        assertEquals("SCHEDULED", result.status());
        assertEquals(MaintenanceStatus.SCHEDULED, newEntity.getStatus());
        assertEquals("admin", newEntity.getCreatedByUser());
    }

    @Test
    void create_withAffectedServices_shouldResolve() {
        UUID serviceId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                "DB Maintenance", "Upgrade", start, end, List.of(serviceId));

        MonitoredService svc = new MonitoredService();
        svc.setId(serviceId);

        MaintenanceWindow newEntity = new MaintenanceWindow();
        MaintenanceWindow saved = createEntity(UUID.randomUUID(), MaintenanceStatus.SCHEDULED);

        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(serviceRepository.findAllById(List.of(serviceId))).thenReturn(List.of(svc));
        when(maintenanceRepository.save(any())).thenReturn(saved);
        when(mapper.toResponse(any())).thenReturn(createResponse(saved.getId(), "SCHEDULED"));

        maintenanceWindowService.create(request, "admin");

        verify(serviceRepository).findAllById(List.of(serviceId));
    }

    @Test
    void findById_shouldReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.SCHEDULED);
        MaintenanceResponse response = createResponse(id, "SCHEDULED");

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        MaintenanceResponse result = maintenanceWindowService.findById(id);

        assertEquals(id, result.id());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(maintenanceRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> maintenanceWindowService.findById(id));
    }

    @Test
    void findAll_byStatus_shouldFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        MaintenanceWindow entity = createEntity(UUID.randomUUID(), MaintenanceStatus.SCHEDULED);

        when(maintenanceRepository.findByStatus(MaintenanceStatus.SCHEDULED)).thenReturn(List.of(entity));
        when(mapper.toResponse(any())).thenReturn(createResponse(entity.getId(), "SCHEDULED"));

        Page<MaintenanceResponse> result = maintenanceWindowService.findAll(MaintenanceStatus.SCHEDULED, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAll_noFilter_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<MaintenanceWindow> page = new PageImpl<>(List.of());

        when(maintenanceRepository.findAll(pageable)).thenReturn(page);

        Page<MaintenanceResponse> result = maintenanceWindowService.findAll(null, pageable);

        assertEquals(0, result.getContent().size());
        verify(maintenanceRepository).findAll(pageable);
    }

    @Test
    void update_shouldPatchFields() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.SCHEDULED);
        Instant newStart = Instant.now().plus(2, ChronoUnit.DAYS);
        MaintenanceUpdateRequest request = new MaintenanceUpdateRequest(
                "Updated Title", "Updated Desc", newStart, null, null);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(maintenanceRepository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(createResponse(id, "SCHEDULED"));

        maintenanceWindowService.update(id, request);

        assertEquals("Updated Title", entity.getTitle());
        assertEquals("Updated Desc", entity.getDescription());
        assertEquals(newStart, entity.getScheduledStart());
    }

    @Test
    void update_nullFields_shouldNotOverwrite() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.SCHEDULED);
        String originalTitle = entity.getTitle();
        MaintenanceUpdateRequest request = new MaintenanceUpdateRequest(
                null, null, null, null, null);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(maintenanceRepository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(createResponse(id, "SCHEDULED"));

        maintenanceWindowService.update(id, request);

        assertEquals(originalTitle, entity.getTitle());
    }

    @Test
    void start_validTransition_shouldSetInProgress() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.SCHEDULED);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(maintenanceRepository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(createResponse(id, "IN_PROGRESS"));

        maintenanceWindowService.start(id);

        assertEquals(MaintenanceStatus.IN_PROGRESS, entity.getStatus());
        assertNotNull(entity.getActualStart());
    }

    @Test
    void start_fromCompleted_shouldThrow() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.COMPLETED);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(InvalidStateTransitionException.class, () -> maintenanceWindowService.start(id));
    }

    @Test
    void complete_validTransition_shouldSetCompleted() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.IN_PROGRESS);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(maintenanceRepository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(createResponse(id, "COMPLETED"));

        maintenanceWindowService.complete(id);

        assertEquals(MaintenanceStatus.COMPLETED, entity.getStatus());
        assertNotNull(entity.getActualEnd());
    }

    @Test
    void complete_fromScheduled_shouldThrow() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.SCHEDULED);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(InvalidStateTransitionException.class, () -> maintenanceWindowService.complete(id));
    }

    @Test
    void cancel_fromScheduled_shouldSucceed() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.SCHEDULED);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(maintenanceRepository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(createResponse(id, "CANCELLED"));

        maintenanceWindowService.cancel(id);

        assertEquals(MaintenanceStatus.CANCELLED, entity.getStatus());
    }

    @Test
    void cancel_fromInProgress_shouldSucceed() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.IN_PROGRESS);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(maintenanceRepository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(createResponse(id, "CANCELLED"));

        maintenanceWindowService.cancel(id);

        assertEquals(MaintenanceStatus.CANCELLED, entity.getStatus());
    }

    @Test
    void cancel_fromCompleted_shouldThrow() {
        UUID id = UUID.randomUUID();
        MaintenanceWindow entity = createEntity(id, MaintenanceStatus.COMPLETED);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(InvalidStateTransitionException.class, () -> maintenanceWindowService.cancel(id));
    }

    @Test
    void findUpcoming_shouldReturnScheduled() {
        MaintenanceWindow entity = createEntity(UUID.randomUUID(), MaintenanceStatus.SCHEDULED);

        when(maintenanceRepository.findByStatus(MaintenanceStatus.SCHEDULED)).thenReturn(List.of(entity));
        when(mapper.toResponse(any())).thenReturn(createResponse(entity.getId(), "SCHEDULED"));

        List<MaintenanceResponse> result = maintenanceWindowService.findUpcoming();

        assertEquals(1, result.size());
    }
}
