-- =============================================================================
-- GPJ — V2: Blockers & Milestones
-- =============================================================================

-- Blockers
CREATE TABLE blockers (
    id          UUID PRIMARY KEY,
    task_id     UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    severity    VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status      VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    resolution  TEXT,
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    version     BIGINT DEFAULT 0
);

CREATE INDEX idx_blockers_task_id ON blockers(task_id);
CREATE INDEX idx_blockers_status ON blockers(status);

-- Milestones
CREATE TABLE milestones (
    id           UUID PRIMARY KEY,
    sprint_id    UUID NOT NULL REFERENCES sprints(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    target_date  DATE NOT NULL,
    status       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    version      BIGINT DEFAULT 0
);

CREATE INDEX idx_milestones_sprint_id ON milestones(sprint_id);
CREATE INDEX idx_milestones_status ON milestones(status);
