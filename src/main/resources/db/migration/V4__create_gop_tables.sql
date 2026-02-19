-- =============================================================================
-- GOP — Gestão de Operações — V4: Create operations monitoring tables
-- =============================================================================

-- 1. Monitored Services
CREATE TABLE monitored_services (
    id                    UUID PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL UNIQUE,
    display_name          VARCHAR(255) NOT NULL,
    type                  VARCHAR(50)  NOT NULL DEFAULT 'BACKEND',
    health_url            VARCHAR(500),
    status                VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN',
    last_check_at         TIMESTAMP WITH TIME ZONE,
    response_time_ms      BIGINT,
    consecutive_failures  INTEGER      NOT NULL DEFAULT 0,
    metadata              JSONB,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITH TIME ZONE,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),
    version               BIGINT DEFAULT 0
);

CREATE INDEX idx_monitored_services_status ON monitored_services(status);
CREATE INDEX idx_monitored_services_type ON monitored_services(type);

-- 2. Health Check Logs
CREATE TABLE health_check_logs (
    id                UUID PRIMARY KEY,
    service_id        UUID NOT NULL REFERENCES monitored_services(id) ON DELETE CASCADE,
    checked_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    status            VARCHAR(50) NOT NULL,
    response_time_ms  BIGINT,
    error_message     TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    version           BIGINT DEFAULT 0
);

CREATE INDEX idx_health_check_logs_service_id ON health_check_logs(service_id);
CREATE INDEX idx_health_check_logs_checked_at ON health_check_logs(checked_at);
CREATE INDEX idx_health_check_logs_service_checked ON health_check_logs(service_id, checked_at);

-- 3. Incidents
CREATE TABLE incidents (
    id            UUID PRIMARY KEY,
    title         VARCHAR(255)  NOT NULL,
    description   TEXT,
    severity      VARCHAR(10)   NOT NULL DEFAULT 'P3',
    status        VARCHAR(50)   NOT NULL DEFAULT 'OPEN',
    reported_by   VARCHAR(255),
    assigned_to   VARCHAR(255),
    resolved_at   TIMESTAMP WITH TIME ZONE,
    root_cause    TEXT,
    resolution    TEXT,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    version       BIGINT DEFAULT 0
);

CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_created_at ON incidents(created_at);

-- 4. Incident Affected Services (many-to-many)
CREATE TABLE incident_affected_services (
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    service_id  UUID NOT NULL REFERENCES monitored_services(id) ON DELETE CASCADE,
    PRIMARY KEY (incident_id, service_id)
);

-- 5. Incident Updates (timeline)
CREATE TABLE incident_updates (
    id          UUID PRIMARY KEY,
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    message     TEXT NOT NULL,
    author      VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    version     BIGINT DEFAULT 0
);

CREATE INDEX idx_incident_updates_incident_id ON incident_updates(incident_id);

-- 6. Deployments
CREATE TABLE deployments (
    id           UUID PRIMARY KEY,
    service_id   UUID NOT NULL REFERENCES monitored_services(id),
    version_tag  VARCHAR(100) NOT NULL,
    commit_hash  VARCHAR(40),
    environment  VARCHAR(50)  NOT NULL DEFAULT 'STAGING',
    deployed_by  VARCHAR(255),
    deployed_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'SUCCESS',
    notes        TEXT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    version      BIGINT DEFAULT 0
);

CREATE INDEX idx_deployments_service_id ON deployments(service_id);
CREATE INDEX idx_deployments_deployed_at ON deployments(deployed_at);
CREATE INDEX idx_deployments_environment ON deployments(environment);

-- 7. Maintenance Windows
CREATE TABLE maintenance_windows (
    id              UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    scheduled_start TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end   TIMESTAMP WITH TIME ZONE NOT NULL,
    actual_start    TIMESTAMP WITH TIME ZONE,
    actual_end      TIMESTAMP WITH TIME ZONE,
    status          VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_by_user VARCHAR(255),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT DEFAULT 0
);

CREATE INDEX idx_maintenance_windows_status ON maintenance_windows(status);
CREATE INDEX idx_maintenance_windows_start ON maintenance_windows(scheduled_start);

-- 8. Maintenance Affected Services (many-to-many)
CREATE TABLE maintenance_affected_services (
    maintenance_id UUID NOT NULL REFERENCES maintenance_windows(id) ON DELETE CASCADE,
    service_id     UUID NOT NULL REFERENCES monitored_services(id) ON DELETE CASCADE,
    PRIMARY KEY (maintenance_id, service_id)
);

-- 9. System Events (from RabbitMQ)
CREATE TABLE system_events (
    id          UUID PRIMARY KEY,
    event_id    UUID NOT NULL UNIQUE,
    source      VARCHAR(50)  NOT NULL,
    event_type  VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   VARCHAR(255),
    payload     JSONB,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    version     BIGINT DEFAULT 0
);

CREATE INDEX idx_system_events_event_id ON system_events(event_id);
CREATE INDEX idx_system_events_source ON system_events(source);
CREATE INDEX idx_system_events_event_type ON system_events(event_type);
CREATE INDEX idx_system_events_timestamp ON system_events(timestamp);

-- Pre-seed monitored services
INSERT INTO monitored_services (id, name, display_name, type, health_url, status, consecutive_failures, created_at, version)
VALUES
  (gen_random_uuid(), 'sgc-backend',  'SGC Backend',  'BACKEND', 'http://localhost:8081/actuator/health', 'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'si-backend',   'SI Backend',   'BACKEND', 'http://localhost:8082/actuator/health', 'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'wn-backend',   'WN Backend',   'BACKEND', 'http://localhost:8083/actuator/health', 'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'gop-backend',  'GOP Backend',  'BACKEND', 'http://localhost:8084/actuator/health', 'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'keycloak',     'Keycloak',     'INFRA',   'http://localhost:8080/health',          'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'postgresql',   'PostgreSQL',   'INFRA',   NULL,                                    'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'redis',        'Redis',        'INFRA',   NULL,                                    'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'rabbitmq',     'RabbitMQ',     'INFRA',   'http://localhost:15672/api/health/checks/alarms', 'UNKNOWN', 0, NOW(), 0),
  (gen_random_uuid(), 'minio',        'MinIO',        'INFRA',   'http://localhost:9000/minio/health/live', 'UNKNOWN', 0, NOW(), 0);
