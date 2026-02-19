package ao.gov.embaixada.gpj.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.gpj.enums.IncidentSeverity;
import ao.gov.embaixada.gpj.enums.IncidentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "incidents")
public class Incident extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IncidentStatus status;

    @Column(name = "reported_by")
    private String reportedBy;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "root_cause", columnDefinition = "text")
    private String rootCause;

    @Column(name = "resolution", columnDefinition = "text")
    private String resolution;

    @ManyToMany
    @JoinTable(
            name = "incident_affected_services",
            joinColumns = @JoinColumn(name = "incident_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<MonitoredService> affectedServices = new HashSet<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<IncidentUpdate> updates = new ArrayList<>();

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Set<MonitoredService> getAffectedServices() {
        return affectedServices;
    }

    public void setAffectedServices(Set<MonitoredService> affectedServices) {
        this.affectedServices = affectedServices;
    }

    public List<IncidentUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<IncidentUpdate> updates) {
        this.updates = updates;
    }
}
