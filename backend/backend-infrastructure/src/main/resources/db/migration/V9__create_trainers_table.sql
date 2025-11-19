-- V9: Create trainers table
-- Trainers represent fitness instructors and personal trainers at the gym

CREATE TABLE IF NOT EXISTS trainers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    branch_id UUID,
    employee_number VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    specializations JSONB,
    certifications JSONB,
    bio TEXT,
    profile_photo_url VARCHAR(500),
    hourly_rate DECIMAL(10, 2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    experience_years INTEGER,
    languages JSONB,
    availability JSONB,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    hire_date DATE NOT NULL,
    termination_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_trainers_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_trainers_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_trainers_organization_id ON trainers(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_trainers_branch_id ON trainers(branch_id) WHERE NOT is_deleted;
CREATE INDEX idx_trainers_email ON trainers(email) WHERE NOT is_deleted;
CREATE INDEX idx_trainers_employee_number ON trainers(employee_number) WHERE NOT is_deleted;
CREATE INDEX idx_trainers_status ON trainers(status) WHERE NOT is_deleted;

-- Unique constraint on employee_number within an organization
CREATE UNIQUE INDEX idx_trainers_org_employee_number_unique ON trainers(organization_id, employee_number) WHERE NOT is_deleted;

-- Unique constraint on email within an organization
CREATE UNIQUE INDEX idx_trainers_org_email_unique ON trainers(organization_id, email) WHERE NOT is_deleted;

-- Add foreign key to class_schedules (instructor_id)
ALTER TABLE class_schedules
    ADD CONSTRAINT fk_class_schedules_instructor FOREIGN KEY (instructor_id)
        REFERENCES trainers(id) ON DELETE SET NULL;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_trainers_updated_at BEFORE UPDATE ON trainers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE trainers IS 'Fitness instructors and personal trainers';
COMMENT ON COLUMN trainers.employee_number IS 'Unique employee identifier within an organization';
COMMENT ON COLUMN trainers.specializations IS 'JSON array of trainer specializations (yoga, crossfit, etc.)';
COMMENT ON COLUMN trainers.certifications IS 'JSON array of certifications with details';
COMMENT ON COLUMN trainers.availability IS 'JSON structure defining trainer availability schedule';
