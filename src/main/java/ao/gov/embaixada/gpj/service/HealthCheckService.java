package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.HealthCheckLogResponse;
import ao.gov.embaixada.gpj.dto.UptimeResponse;
import ao.gov.embaixada.gpj.entity.HealthCheckLog;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.integration.GopEventPublisher;
import ao.gov.embaixada.gpj.mapper.HealthCheckLogMapper;
import ao.gov.embaixada.gpj.repository.HealthCheckLogRepository;
import ao.gov.embaixada.gpj.repository.MonitoredServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final MonitoredServiceRepository serviceRepository;
    private final HealthCheckLogRepository healthCheckLogRepository;
    private final HealthCheckLogMapper healthCheckLogMapper;
    private final RestClient healthCheckRestClient;
    private final IncidentService incidentService;
    private final GopEventPublisher eventPublisher;

    @Value("${gop.health-poller.enabled:false}")
    private boolean pollerEnabled;

    @Value("${gop.health-poller.failure-threshold:3}")
    private int failureThreshold;

    @Value("${gop.health-poller.retention-days:90}")
    private int retentionDays;

    public HealthCheckService(MonitoredServiceRepository serviceRepository,
                              HealthCheckLogRepository healthCheckLogRepository,
                              HealthCheckLogMapper healthCheckLogMapper,
                              @Qualifier("healthCheckRestClient") RestClient healthCheckRestClient,
                              IncidentService incidentService,
                              GopEventPublisher eventPublisher) {
        this.serviceRepository = serviceRepository;
        this.healthCheckLogRepository = healthCheckLogRepository;
        this.healthCheckLogMapper = healthCheckLogMapper;
        this.healthCheckRestClient = healthCheckRestClient;
        this.incidentService = incidentService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRateString = "${gop.health-poller.interval-ms:30000}")
    public void pollAllServices() {
        if (!pollerEnabled) {
            return;
        }

        log.debug("Starting health check poll for all services");
        List<MonitoredService> services = serviceRepository.findAll();

        for (MonitoredService service : services) {
            if (service.getHealthUrl() == null || service.getHealthUrl().isBlank()) {
                continue;
            }
            try {
                checkService(service);
            } catch (Exception ex) {
                log.error("Unexpected error polling service {}: {}", service.getName(), ex.getMessage(), ex);
            }
        }
    }

    private void checkService(MonitoredService service) {
        Instant checkTime = Instant.now();
        ServiceStatus previousStatus = service.getStatus();
        long startMs = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = healthCheckRestClient.get()
                    .uri(service.getHealthUrl())
                    .retrieve()
                    .toEntity(String.class);

            long elapsed = System.currentTimeMillis() - startMs;

            if (response.getStatusCode().is2xxSuccessful()) {
                handleSuccess(service, checkTime, elapsed, previousStatus);
            } else {
                handleFailure(service, checkTime, elapsed,
                        "Non-2xx status: " + response.getStatusCode(), previousStatus);
            }
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - startMs;
            handleFailure(service, checkTime, elapsed, ex.getMessage(), previousStatus);
        }
    }

    private void handleSuccess(MonitoredService service, Instant checkTime, long elapsed,
                                ServiceStatus previousStatus) {
        service.setStatus(ServiceStatus.UP);
        service.setConsecutiveFailures(0);
        service.setResponseTimeMs(elapsed);
        service.setLastCheckAt(checkTime);
        serviceRepository.save(service);

        HealthCheckLog logEntry = new HealthCheckLog();
        logEntry.setService(service);
        logEntry.setCheckedAt(checkTime);
        logEntry.setStatus(ServiceStatus.UP);
        logEntry.setResponseTimeMs(elapsed);
        healthCheckLogRepository.save(logEntry);

        if (previousStatus == ServiceStatus.DOWN) {
            log.info("Service recovered: {} (was DOWN, now UP)", service.getDisplayName());
            eventPublisher.serviceRecovered(service.getDisplayName());
        }
    }

    private void handleFailure(MonitoredService service, Instant checkTime, long elapsed,
                                String errorMessage, ServiceStatus previousStatus) {
        service.setStatus(ServiceStatus.DOWN);
        service.setConsecutiveFailures(service.getConsecutiveFailures() + 1);
        service.setResponseTimeMs(elapsed);
        service.setLastCheckAt(checkTime);
        serviceRepository.save(service);

        HealthCheckLog logEntry = new HealthCheckLog();
        logEntry.setService(service);
        logEntry.setCheckedAt(checkTime);
        logEntry.setStatus(ServiceStatus.DOWN);
        logEntry.setResponseTimeMs(elapsed);
        logEntry.setErrorMessage(errorMessage);
        healthCheckLogRepository.save(logEntry);

        log.warn("Service health check failed: {} (consecutiveFailures={})",
                service.getDisplayName(), service.getConsecutiveFailures());

        if (service.getConsecutiveFailures() >= failureThreshold) {
            incidentService.autoCreateFromHealthFailure(service);
        }
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckLogResponse> getHealthHistory(UUID serviceId, Pageable pageable) {
        return healthCheckLogRepository.findByServiceIdOrderByCheckedAtDesc(serviceId, pageable)
                .map(healthCheckLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UptimeResponse calculateUptime(UUID serviceId) {
        MonitoredService service = serviceRepository.findById(serviceId)
                .orElse(null);
        if (service == null) {
            return new UptimeResponse(serviceId, "Unknown", 0.0, 0.0, 0.0);
        }

        double uptime24h = calculateUptimeForPeriod(serviceId, 24);
        double uptime7d = calculateUptimeForPeriod(serviceId, 24 * 7);
        double uptime30d = calculateUptimeForPeriod(serviceId, 24 * 30);

        return new UptimeResponse(
                serviceId,
                service.getDisplayName(),
                uptime24h,
                uptime7d,
                uptime30d
        );
    }

    private double calculateUptimeForPeriod(UUID serviceId, int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        long totalChecks = healthCheckLogRepository.countByServiceIdAndCheckedAtAfter(serviceId, since);
        if (totalChecks == 0) {
            return 100.0;
        }
        long upChecks = healthCheckLogRepository.countByServiceIdAndStatusAndCheckedAtAfter(
                serviceId, ServiceStatus.UP, since);
        return Math.round((double) upChecks / totalChecks * 10000.0) / 100.0;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldLogs() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        healthCheckLogRepository.deleteByCheckedAtBefore(cutoff);
        log.info("Cleaned up health check logs older than {} days", retentionDays);
    }
}
