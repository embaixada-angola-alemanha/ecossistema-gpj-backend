package ao.gov.embaixada.gpj.integration;

import ao.gov.embaixada.commons.integration.event.Exchanges;
import ao.gov.embaixada.commons.integration.event.IntegrationEvent;
import ao.gov.embaixada.gpj.service.SystemEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GopEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(GopEventConsumer.class);

    private final SystemEventService systemEventService;

    public GopEventConsumer(SystemEventService systemEventService) {
        this.systemEventService = systemEventService;
    }

    @RabbitListener(queues = Exchanges.QUEUE_GPJ_MONITOR, concurrency = "1-5")
    public void handleEvent(IntegrationEvent event) {
        log.info("[GOP Monitor] Event: source={}, type={}, entity={}/{}",
                event.source(), event.eventType(), event.entityType(), event.entityId());
        systemEventService.persistEvent(event);
    }
}
