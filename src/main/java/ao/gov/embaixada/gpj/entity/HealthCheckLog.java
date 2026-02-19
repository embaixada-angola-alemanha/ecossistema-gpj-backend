package ao.gov.embaixada.gpj.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "health_check_logs")
public class HealthCheckLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MonitoredService service;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServiceStatus status;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    // Getters and Setters

    public MonitoredService getService() {
        return service;
    }

    public void setService(MonitoredService service) {
        this.service = service;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(Instant checkedAt) {
        this.checkedAt = checkedAt;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
