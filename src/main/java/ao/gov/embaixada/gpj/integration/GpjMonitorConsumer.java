package ao.gov.embaixada.gpj.integration;

import ao.gov.embaixada.commons.integration.event.Exchanges;
import ao.gov.embaixada.commons.integration.event.IntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * GPJ↔ALL: Monitors ALL cross-system events for project management visibility.
 * Subscribes to the '#' routing key to receive every integration event.
 * Logs, tracks, and can create monitoring dashboards/alerts.
 */
@Component
public class GpjMonitorConsumer {

    private static final Logger log = LoggerFactory.getLogger(GpjMonitorConsumer.class);

    @RabbitListener(queues = Exchanges.QUEUE_GPJ_MONITOR, concurrency = "1-5")
    public void handleEvent(IntegrationEvent event) {
        log.info("[GPJ Monitor] Event received: source={}, type={}, entity={}/{}, timestamp={}",
            event.source(), event.eventType(), event.entityType(), event.entityId(), event.timestamp());

        trackSystemActivity(event);
    }

    /**
     * Track system-wide activity for project management dashboards.
     * Provides cross-system visibility: how many events each system produces,
     * what activities are happening, and whether systems are healthy.
     */
    private void trackSystemActivity(IntegrationEvent event) {
        String source = event.source();
        String type = event.eventType();

        // Categorize by system for dashboard metrics
        switch (source) {
            case "SGC" -> trackSgcActivity(event);
            case "SI" -> trackSiActivity(event);
            case "WN" -> trackWnActivity(event);
            case "GPJ" -> log.debug("GPJ self-event: {}", type);
            default -> log.warn("Unknown event source: {}", source);
        }
    }

    private void trackSgcActivity(IntegrationEvent event) {
        // TODO: Persist to GPJ monitoring table for dashboards
        // MonitoringService.recordEvent(event.source(), event.eventType(), event.entityType(), event.timestamp())
        log.info("[GPJ Monitor] SGC activity: {} on {}", event.eventType(), event.entityType());
    }

    private void trackSiActivity(IntegrationEvent event) {
        log.info("[GPJ Monitor] SI activity: {} on {}", event.eventType(), event.entityType());
    }

    private void trackWnActivity(IntegrationEvent event) {
        log.info("[GPJ Monitor] WN activity: {} on {}", event.eventType(), event.entityType());
    }
}
