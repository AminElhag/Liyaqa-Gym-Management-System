-- Initialize Liyaqa Gym Management Database

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS gym;
CREATE SCHEMA IF NOT EXISTS audit;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA gym TO liyaqa_admin;
GRANT ALL PRIVILEGES ON SCHEMA audit TO liyaqa_admin;

-- Set default schema
ALTER DATABASE liyaqa_gym SET search_path TO gym, public;

-- Create audit table
CREATE TABLE IF NOT EXISTS audit.audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    old_data JSONB,
    new_data JSONB,
    user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on audit log
CREATE INDEX IF NOT EXISTS idx_audit_log_table_name ON audit.audit_log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit.audit_log(created_at);

COMMENT ON DATABASE liyaqa_gym IS 'Liyaqa Gym Management System Database';
