-- V10: Create pt_sessions table
-- PT (Personal Training) sessions represent one-on-one training appointments

CREATE TABLE IF NOT EXISTS pt_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    trainer_id UUID NOT NULL,
    member_id UUID NOT NULL,
    session_number VARCHAR(50) NOT NULL,
    scheduled_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    session_type VARCHAR(50) NOT NULL DEFAULT 'PERSONAL_TRAINING',
    focus_areas JSONB,
    notes TEXT,
    trainer_notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    check_in_time TIMESTAMP,
    completion_time TIMESTAMP,
    cancellation_time TIMESTAMP,
    cancellation_reason TEXT,
    no_show BOOLEAN NOT NULL DEFAULT FALSE,
    amount DECIMAL(10, 2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pt_sessions_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pt_sessions_trainer FOREIGN KEY (trainer_id)
        REFERENCES trainers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pt_sessions_member FOREIGN KEY (member_id)
        REFERENCES members(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_pt_sessions_organization_id ON pt_sessions(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_pt_sessions_trainer_id ON pt_sessions(trainer_id) WHERE NOT is_deleted;
CREATE INDEX idx_pt_sessions_member_id ON pt_sessions(member_id) WHERE NOT is_deleted;
CREATE INDEX idx_pt_sessions_scheduled_date ON pt_sessions(scheduled_date) WHERE NOT is_deleted;
CREATE INDEX idx_pt_sessions_status ON pt_sessions(status) WHERE NOT is_deleted;
CREATE INDEX idx_pt_sessions_session_number ON pt_sessions(session_number) WHERE NOT is_deleted;
CREATE INDEX idx_pt_sessions_date_time ON pt_sessions(scheduled_date, start_time) WHERE NOT is_deleted;

-- Unique constraint on session_number within an organization
CREATE UNIQUE INDEX idx_pt_sessions_org_number_unique ON pt_sessions(organization_id, session_number) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_pt_sessions_updated_at BEFORE UPDATE ON pt_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE pt_sessions IS 'Personal training sessions between trainers and members';
COMMENT ON COLUMN pt_sessions.session_number IS 'Unique session identifier within an organization';
COMMENT ON COLUMN pt_sessions.session_type IS 'Type of session (PERSONAL_TRAINING, ASSESSMENT, CONSULTATION)';
COMMENT ON COLUMN pt_sessions.focus_areas IS 'JSON array of focus areas (strength, cardio, flexibility, etc.)';
COMMENT ON COLUMN pt_sessions.status IS 'Session status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)';
COMMENT ON COLUMN pt_sessions.trainer_notes IS 'Private notes from trainer about the session';
