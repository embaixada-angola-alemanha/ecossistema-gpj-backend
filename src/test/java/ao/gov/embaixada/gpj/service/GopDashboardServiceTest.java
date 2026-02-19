package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.*;
import ao.gov.embaixada.gpj.entity.Incident;
import ao.gov.embaixada.gpj.enums.IncidentSeverity;
import ao.gov.embaixada.gpj.enums.IncidentStatus;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.repository.IncidentRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class GopDashboardServiceTest {

    @Mock
    private MonitoredServiceRepository serviceRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private MaintenanceWindowService maintenanceWindowService;

    @Mock
    private SystemEventService systemEventService;

    @Mock
    private HealthCheckService healthCheckService;

    @Mock
    private MonitoredServiceService monitoredServiceService;

    @InjectMocks
    private GopDashboardService dashboardService;

    @Test
    void getDashboard_shouldAggregateAllData() {
        when(serviceRepository.countByStatus(ServiceStatus.UP)).thenReturn(5L);
        when(serviceRepository.countByStatus(ServiceStatus.DOWN)).thenReturn(1L);
        when(serviceRepository.countByStatus(ServiceStatus.DEGRADED)).thenReturn(0L);
        when(incidentRepository.countByStatusIn(anyList())).thenReturn(2L);

        // Mock the P1 incident count — the service calls findByStatus for each active status
        Incident p1Incident = new Incident();
        p1Incident.setSeverity(IncidentSeverity.P1);
        Incident p2Incident = new Incident();
        p2Incident.setSeverity(IncidentSeverity.P2);

        Page<Incident> pageWithP1 = new PageImpl<>(List.of(p1Incident, p2Incident));
        Page<Incident> emptyPage = new PageImpl<>(List.of());

        when(incidentRepository.findByStatus(eq(IncidentStatus.OPEN), any(Pageable.class))).thenReturn(pageWithP1);
        when(incidentRepository.findByStatus(eq(IncidentStatus.INVESTIGATING), any(Pageable.class))).thenReturn(emptyPage);
        when(incidentRepository.findByStatus(eq(IncidentStatus.IDENTIFIED), any(Pageable.class))).thenReturn(emptyPage);
        when(incidentRepository.findByStatus(eq(IncidentStatus.MONITORING), any(Pageable.class))).thenReturn(emptyPage);

        when(deploymentService.findRecent()).thenReturn(List.of());
        when(maintenanceWindowService.findUpcoming()).thenReturn(List.of());
        when(systemEventService.countToday()).thenReturn(10L);

        GopDashboardResponse result = dashboardService.getDashboard();

        assertEquals(5L, result.servicesUp());
        assertEquals(1L, result.servicesDown());
        assertEquals(0L, result.servicesDegraded());
        assertEquals(2L, result.activeIncidents());
        assertEquals(1L, result.p1Incidents());
        assertEquals(10L, result.eventsToday());
        assertTrue(result.recentDeployments().isEmpty());
        assertTrue(result.upcomingMaintenance().isEmpty());
    }

    @Test
    void getDashboard_noP1Incidents_shouldReturnZero() {
        when(serviceRepository.countByStatus(any())).thenReturn(0L);
        when(incidentRepository.countByStatusIn(anyList())).thenReturn(0L);

        Page<Incident> emptyPage = new PageImpl<>(List.of());
        when(incidentRepository.findByStatus(any(IncidentStatus.class), any(Pageable.class))).thenReturn(emptyPage);

        when(deploymentService.findRecent()).thenReturn(List.of());
        when(maintenanceWindowService.findUpcoming()).thenReturn(List.of());
        when(systemEventService.countToday()).thenReturn(0L);

        GopDashboardResponse result = dashboardService.getDashboard();

        assertEquals(0L, result.p1Incidents());
    }

    @Test
    void getUptimeAll_shouldReturnUptimePerService() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        MonitoredServiceResponse svc1 = new MonitoredServiceResponse(
                id1, "sgc", "SGC Backend", "BACKEND", "UP", null, null, 0, Instant.now());
        MonitoredServiceResponse svc2 = new MonitoredServiceResponse(
                id2, "si", "SI Backend", "BACKEND", "UP", null, null, 0, Instant.now());

        UptimeResponse up1 = new UptimeResponse(id1, "SGC Backend", 99.9, 99.5, 99.0);
        UptimeResponse up2 = new UptimeResponse(id2, "SI Backend", 100.0, 100.0, 100.0);

        when(monitoredServiceService.findAll()).thenReturn(List.of(svc1, svc2));
        when(healthCheckService.calculateUptime(id1)).thenReturn(up1);
        when(healthCheckService.calculateUptime(id2)).thenReturn(up2);

        List<UptimeResponse> result = dashboardService.getUptimeAll();

        assertEquals(2, result.size());
        assertEquals(99.9, result.get(0).uptime24h());
        assertEquals(100.0, result.get(1).uptime24h());
    }

    @Test
    void getUptimeAll_noServices_shouldReturnEmptyList() {
        when(monitoredServiceService.findAll()).thenReturn(List.of());

        List<UptimeResponse> result = dashboardService.getUptimeAll();

        assertTrue(result.isEmpty());
    }
}
