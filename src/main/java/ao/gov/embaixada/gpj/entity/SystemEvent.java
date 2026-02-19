package ao.gov.embaixada.gpj.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_events")
public class SystemEvent extends BaseEntity {

    @Column(name = "event_id", unique = true, nullable = false)
    private UUID eventId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed")
    private boolean processed = false;

    // Getters and Setters

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
