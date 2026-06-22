-- V2__add_analyses_composite_index.sql
-- Añadir índice compuesto para optimizar las consultas analíticas del Dashboard Analytics

CREATE INDEX idx_analyses_project_status_created 
ON analyses (project_id, status, created_at);
