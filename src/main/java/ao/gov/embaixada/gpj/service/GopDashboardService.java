package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.DeploymentResponse;
import ao.gov.embaixada.gpj.dto.GopDashboardResponse;
import ao.gov.embaixada.gpj.dto.MaintenanceResponse;
import ao.gov.embaixada.gpj.dto.MonitoredServiceResponse;
import ao.gov.embaixada.gpj.dto.UptimeResponse;
import ao.gov.embaixada.gpj.enums.IncidentSeverity;
import ao.gov.embaixada.gpj.enums.IncidentStatus;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.repository.IncidentRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GopDashboardService {

    private static final Logger log = LoggerFactory.getLogger(GopDashboardService.class);

    private final MonitoredServiceRepository serviceRepository;
    private final IncidentRepository incidentRepository;
    private final DeploymentService deploymentService;
    private final MaintenanceWindowService maintenanceWindowService;
    private final SystemEventService systemEventService;
    private final HealthCheckService healthCheckService;
    private final MonitoredServiceService monitoredServiceService;

    public GopDashboardService(MonitoredServiceRepository serviceRepository,
                               IncidentRepository incidentRepository,
                               DeploymentService deploymentService,
                               MaintenanceWindowService maintenanceWindowService,
                               SystemEventService systemEventService,
                               HealthCheckService healthCheckService,
                               MonitoredServiceService monitoredServiceService) {
        this.serviceRepository = serviceRepository;
        this.incidentRepository = incidentRepository;
        this.deploymentService = deploymentService;
        this.maintenanceWindowService = maintenanceWindowService;
        this.systemEventService = systemEventService;
        this.healthCheckService = healthCheckService;
        this.monitoredServiceService = monitoredServiceService;
    }

    @Transactional(readOnly = true)
    public GopDashboardResponse getDashboard() {
        long servicesUp = serviceRepository.countByStatus(ServiceStatus.UP);
        long servicesDown = serviceRepository.countByStatus(ServiceStatus.DOWN);
        long servicesDegraded = serviceRepository.countByStatus(ServiceStatus.DEGRADED);

        List<IncidentStatus> activeStatuses = List.of(
                IncidentStatus.OPEN,
                IncidentStatus.INVESTIGATING,
                IncidentStatus.IDENTIFIED,
                IncidentStatus.MONITORING
        );
        long activeIncidents = incidentRepository.countByStatusIn(activeStatuses);

        // Count P1 incidents that are in active statuses
        // Since the repository doesn't have a combined query, we use countByStatusIn for P1 statuses
        // We'll approximate by counting all active P1 incidents from the active statuses
        long p1Incidents = incidentRepository.findByStatus(IncidentStatus.OPEN,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(i -> i.getSeverity() == IncidentSeverity.P1)
                .count()
                + incidentRepository.findByStatus(IncidentStatus.INVESTIGATING,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(i -> i.getSeverity() == IncidentSeverity.P1)
                .count()
                + incidentRepository.findByStatus(IncidentStatus.IDENTIFIED,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(i -> i.getSeverity() == IncidentSeverity.P1)
                .count()
                + incidentRepository.findByStatus(IncidentStatus.MONITORING,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(i -> i.getSeverity() == IncidentSeverity.P1)
                .count();

        List<DeploymentResponse> recentDeployments = deploymentService.findRecent();
        List<MaintenanceResponse> upcomingMaintenance = maintenanceWindowService.findUpcoming();
        long eventsToday = systemEventService.countToday();

        return new GopDashboardResponse(
                servicesUp,
                servicesDown,
                servicesDegraded,
                activeIncidents,
                p1Incidents,
                recentDeployments,
                upcomingMaintenance,
                eventsToday
        );
    }

    @Transactional(readOnly = true)
    public List<UptimeResponse> getUptimeAll() {
        List<MonitoredServiceResponse> allServices = monitoredServiceService.findAll();
        return allServices.stream()
                .map(s -> healthCheckService.calculateUptime(s.id()))
                .toList();
    }
}
