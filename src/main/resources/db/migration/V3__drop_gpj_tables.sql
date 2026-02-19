-- =============================================================================
-- GOP Transformation — V3: Drop all GPJ (project management) tables
-- =============================================================================
DROP TABLE IF EXISTS blockers CASCADE;
DROP TABLE IF EXISTS milestones CASCADE;
DROP TABLE IF EXISTS time_logs CASCADE;
DROP TABLE IF EXISTS task_dependencies CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS sprints CASCADE;
