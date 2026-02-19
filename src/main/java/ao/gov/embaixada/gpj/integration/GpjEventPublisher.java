package ao.gov.embaixada.gpj.integration;

import ao.gov.embaixada.commons.integration.IntegrationEventPublisher;
import ao.gov.embaixada.commons.integration.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Publishes GPJ events to the cross-system integration exchange.
 * Other systems can react to project milestone changes.
 */
@Service
public class GpjEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GpjEventPublisher.class);

    private final IntegrationEventPublisher publisher;

    public GpjEventPublisher(@Nullable IntegrationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void taskStatusChanged(String taskId, String titulo, String previousStatus, String newStatus) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_GPJ, EventTypes.GPJ_TASK_STATUS_CHANGED, taskId, "Task",
            Map.of("titulo", titulo, "previousStatus", previousStatus, "newStatus", newStatus));
    }

    public void sprintCompleted(String sprintId, String nome, int totalTarefas, int tarefasConcluidas) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_GPJ, EventTypes.GPJ_SPRINT_COMPLETED, sprintId, "Sprint",
            Map.of("nome", nome, "totalTarefas", totalTarefas, "tarefasConcluidas", tarefasConcluidas));
        log.info("Published GPJ sprint completed: {}", nome);
    }

    public void milestoneReached(String milestoneId, String nome) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_GPJ, EventTypes.GPJ_MILESTONE_REACHED, milestoneId, "Milestone",
            Map.of("nome", nome));
    }
}
