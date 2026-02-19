package ao.gov.embaixada.gpj.integration;

import ao.gov.embaixada.commons.integration.IntegrationEventPublisher;
import ao.gov.embaixada.commons.integration.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class GopEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GopEventPublisher.class);

    private final IntegrationEventPublisher publisher;

    public GopEventPublisher(@Nullable IntegrationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void incidentCreated(UUID incidentId, String title, String severity) {
        if (publisher == null) {
            log.debug("IntegrationEventPublisher not available, skipping incidentCreated event");
            return;
        }
        try {
            publisher.publish(
                    EventTypes.SOURCE_GOP,
                    EventTypes.GOP_INCIDENT_CREATED,
                    incidentId.toString(),
                    "Incident",
                    Map.of("title", title, "severity", severity)
            );
            log.info("Published GOP_INCIDENT_CREATED: id={}, title={}", incidentId, title);
        } catch (Exception ex) {
            log.warn("Failed to publish GOP_INCIDENT_CREATED event: {}", ex.getMessage());
        }
    }

    public void incidentResolved(UUID incidentId, String title) {
        if (publisher == null) {
            log.debug("IntegrationEventPublisher not available, skipping incidentResolved event");
            return;
        }
        try {
            publisher.publish(
                    EventTypes.SOURCE_GOP,
                    EventTypes.GOP_INCIDENT_RESOLVED,
                    incidentId.toString(),
                    "Incident",
                    Map.of("title", title)
            );
            log.info("Published GOP_INCIDENT_RESOLVED: id={}, title={}", incidentId, title);
        } catch (Exception ex) {
            log.warn("Failed to publish GOP_INCIDENT_RESOLVED event: {}", ex.getMessage());
        }
    }

    public void serviceDown(String serviceName) {
        if (publisher == null) {
            log.debug("IntegrationEventPublisher not available, skipping serviceDown event");
            return;
        }
        try {
            publisher.publish(
                    EventTypes.SOURCE_GOP,
                    EventTypes.GOP_SERVICE_DOWN,
                    serviceName,
                    "MonitoredService",
                    Map.of("serviceName", serviceName)
            );
            log.info("Published GOP_SERVICE_DOWN: service={}", serviceName);
        } catch (Exception ex) {
            log.warn("Failed to publish GOP_SERVICE_DOWN event: {}", ex.getMessage());
        }
    }

    public void serviceRecovered(String serviceName) {
        if (publisher == null) {
            log.debug("IntegrationEventPublisher not available, skipping serviceRecovered event");
            return;
        }
        try {
            publisher.publish(
                    EventTypes.SOURCE_GOP,
                    EventTypes.GOP_SERVICE_RECOVERED,
                    serviceName,
                    "MonitoredService",
                    Map.of("serviceName", serviceName)
            );
            log.info("Published GOP_SERVICE_RECOVERED: service={}", serviceName);
        } catch (Exception ex) {
            log.warn("Failed to publish GOP_SERVICE_RECOVERED event: {}", ex.getMessage());
        }
    }
}
