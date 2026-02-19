package ao.gov.embaixada.gpj.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.enums.ServiceType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monitored_services")
public class MonitoredService extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ServiceType type;

    @Column(name = "health_url")
    private String healthUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status;

    @Column(name = "last_check_at")
    private Instant lastCheckAt;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "consecutive_failures")
    private int consecutiveFailures = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @OneToMany(mappedBy = "service")
    private List<HealthCheckLog> healthChecks = new ArrayList<>();

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public String getHealthUrl() {
        return healthUrl;
    }

    public void setHealthUrl(String healthUrl) {
        this.healthUrl = healthUrl;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public Instant getLastCheckAt() {
        return lastCheckAt;
    }

    public void setLastCheckAt(Instant lastCheckAt) {
        this.lastCheckAt = lastCheckAt;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public List<HealthCheckLog> getHealthChecks() {
        return healthChecks;
    }

    public void setHealthChecks(List<HealthCheckLog> healthChecks) {
        this.healthChecks = healthChecks;
    }
}
