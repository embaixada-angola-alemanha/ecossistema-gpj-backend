package ao.gov.embaixada.gpj.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.gpj.enums.DeploymentEnvironment;
import ao.gov.embaixada.gpj.enums.DeploymentStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "deployments")
public class Deployment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MonitoredService service;

    @Column(name = "version_tag", nullable = false)
    private String versionTag;

    @Column(name = "commit_hash")
    private String commitHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private DeploymentEnvironment environment;

    @Column(name = "deployed_by")
    private String deployedBy;

    @Column(name = "deployed_at", nullable = false)
    private Instant deployedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeploymentStatus status;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    // Getters and Setters

    public MonitoredService getService() {
        return service;
    }

    public void setService(MonitoredService service) {
        this.service = service;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public DeploymentEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(DeploymentEnvironment environment) {
        this.environment = environment;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public Instant getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(Instant deployedAt) {
        this.deployedAt = deployedAt;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
