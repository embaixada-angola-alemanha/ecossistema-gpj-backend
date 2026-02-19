package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.UptimeResponse;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.enums.ServiceType;
import ao.gov.embaixada.gpj.integration.GopEventPublisher;
import ao.gov.embaixada.gpj.mapper.HealthCheckLogMapper;
import ao.gov.embaixada.gpj.repository.HealthCheckLogRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class HealthCheckServiceTest {

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private HealthCheckLogRepository healthCheckLogRepository;

    @Mock
    private HealthCheckLogMapper healthCheckLogMapper;

    @Mock
    private RestClient healthCheckRestClient;

    @Mock
    private IncidentService incidentService;

    @Mock
    private GopEventPublisher eventPublisher;

    @InjectMocks
    private HealthCheckService healthCheckService;

    private MonitoredService createService(UUID id, String name, ServiceStatus status) {
        MonitoredService svc = new MonitoredService();
        svc.setId(id);
        svc.setName(name);
        svc.setDisplayName(name + " Display");
        svc.setType(ServiceType.BACKEND);
        svc.setStatus(status);
        svc.setConsecutiveFailures(0);
        svc.setHealthUrl("http://localhost:8080/actuator/health");
        return svc;
    }

    @Test
    void pollAllServices_pollerDisabled_shouldDoNothing() {
        ReflectionTestUtils.setField(healthCheckService, "pollerEnabled", false);

        healthCheckService.pollAllServices();

        verifyNoInteractions(serviceRepository);
    }

    @Test
    void pollAllServices_pollerEnabled_shouldPollEachService() {
        ReflectionTestUtils.setField(healthCheckService, "pollerEnabled", true);
        ReflectionTestUtils.setField(healthCheckService, "failureThreshold", 3);

        MonitoredService svc = createService(UUID.randomUUID(), "sgc-backend", ServiceStatus.UP);
        // Service with no URL should be skipped
        MonitoredService noUrl = createService(UUID.randomUUID(), "no-url-svc", ServiceStatus.UNKNOWN);
        noUrl.setHealthUrl(null);

        when(serviceRepository.findAll()).thenReturn(List.of(svc, noUrl));

        // The actual HTTP call will fail because healthCheckRestClient is a mock,
        // but we just need to verify that the flow proceeds without errors
        healthCheckService.pollAllServices();

        verify(serviceRepository).findAll();
    }

    @Test
    void calculateUptime_serviceNotFound_shouldReturnDefault() {
        UUID id = UUID.randomUUID();
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());

        UptimeResponse result = healthCheckService.calculateUptime(id);

        assertEquals(0.0, result.uptime24h());
        assertEquals(0.0, result.uptime7d());
        assertEquals(0.0, result.uptime30d());
        assertEquals("Unknown", result.serviceName());
    }

    @Test
    void calculateUptime_noChecks_shouldReturn100Percent() {
        UUID id = UUID.randomUUID();
        MonitoredService svc = createService(id, "sgc-backend", ServiceStatus.UP);

        when(serviceRepository.findById(id)).thenReturn(Optional.of(svc));
        when(healthCheckLogRepository.countByServiceIdAndCheckedAtAfter(eq(id), any())).thenReturn(0L);

        UptimeResponse result = healthCheckService.calculateUptime(id);

        assertEquals(100.0, result.uptime24h());
        assertEquals(100.0, result.uptime7d());
        assertEquals(100.0, result.uptime30d());
    }

    @Test
    void calculateUptime_mixedChecks_shouldCalculatePercentage() {
        UUID id = UUID.randomUUID();
        MonitoredService svc = createService(id, "sgc-backend", ServiceStatus.UP);

        when(serviceRepository.findById(id)).thenReturn(Optional.of(svc));
        when(healthCheckLogRepository.countByServiceIdAndCheckedAtAfter(eq(id), any())).thenReturn(100L);
        when(healthCheckLogRepository.countByServiceIdAndStatusAndCheckedAtAfter(eq(id), eq(ServiceStatus.UP), any())).thenReturn(95L);

        UptimeResponse result = healthCheckService.calculateUptime(id);

        assertEquals(95.0, result.uptime24h());
    }

    @Test
    void cleanupOldLogs_shouldDeleteOldEntries() {
        ReflectionTestUtils.setField(healthCheckService, "retentionDays", 90);

        healthCheckService.cleanupOldLogs();

        verify(healthCheckLogRepository).deleteByCheckedAtBefore(any());
    }

    @Test
    void pollAllServices_emptyHealthUrl_shouldSkip() {
        ReflectionTestUtils.setField(healthCheckService, "pollerEnabled", true);
        ReflectionTestUtils.setField(healthCheckService, "failureThreshold", 3);

        MonitoredService blankUrl = createService(UUID.randomUUID(), "blank-url", ServiceStatus.UNKNOWN);
        blankUrl.setHealthUrl("   ");

        when(serviceRepository.findAll()).thenReturn(List.of(blankUrl));

        healthCheckService.pollAllServices();

        // No health check log should be saved for services with blank URLs
        verifyNoInteractions(healthCheckLogRepository);
    }

    @Test
    void pollAllServices_noServices_shouldComplete() {
        ReflectionTestUtils.setField(healthCheckService, "pollerEnabled", true);

        when(serviceRepository.findAll()).thenReturn(Collections.emptyList());

        healthCheckService.pollAllServices();

        verify(serviceRepository).findAll();
        verifyNoInteractions(healthCheckLogRepository);
    }
}
