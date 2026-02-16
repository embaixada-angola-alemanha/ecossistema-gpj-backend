-- =============================================================================
-- GPJ — Gestão de Projectos — V1 Schema
-- =============================================================================

-- Sprints
CREATE TABLE sprints (
    id          UUID PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    start_date  DATE,
    end_date    DATE,
    status      VARCHAR(50) NOT NULL DEFAULT 'PLANNING',
    capacity_hours DOUBLE PRECISION DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    version     BIGINT DEFAULT 0
);

-- Tasks
CREATE TABLE tasks (
    id              UUID PRIMARY KEY,
    sprint_id       UUID REFERENCES sprints(id) ON DELETE SET NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(50) NOT NULL DEFAULT 'BACKLOG',
    priority        VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    assignee        VARCHAR(255),
    estimated_hours DOUBLE PRECISION DEFAULT 0,
    consumed_hours  DOUBLE PRECISION DEFAULT 0,
    progress_pct    INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT DEFAULT 0
);

CREATE INDEX idx_tasks_sprint_id ON tasks(sprint_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_assignee ON tasks(assignee);

-- Task dependencies (many-to-many self-referencing)
CREATE TABLE task_dependencies (
    task_id       UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    depends_on_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, depends_on_id),
    CHECK (task_id != depends_on_id)
);

-- Time logs
CREATE TABLE time_logs (
    id          UUID PRIMARY KEY,
    task_id     UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id     VARCHAR(255) NOT NULL,
    hours       DOUBLE PRECISION NOT NULL,
    description TEXT,
    log_date    DATE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    version     BIGINT DEFAULT 0
);

CREATE INDEX idx_time_logs_task_id ON time_logs(task_id);
