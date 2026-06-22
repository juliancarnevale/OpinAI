-- V1__init_schema.sql
-- Migración inicial para OpinAI: Estructura base de base de datos en PostgreSQL

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_projects_user_id ON projects(user_id);

CREATE TABLE analyses (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    overall_sentiment VARCHAR(50),
    executive_summary TEXT,
    key_issues JSONB,
    improvement_opportunities JSONB,
    sentiment_distribution JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_analyses_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_analyses_project_id ON analyses(project_id);

CREATE TABLE feedback_items (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL,
    content TEXT NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    external_metadata VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_feedback_items_analysis FOREIGN KEY (analysis_id) REFERENCES analyses(id) ON DELETE CASCADE
);

CREATE INDEX idx_feedback_items_analysis_id ON feedback_items(analysis_id);
