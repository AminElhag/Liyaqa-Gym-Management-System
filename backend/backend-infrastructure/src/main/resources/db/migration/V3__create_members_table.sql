-- V3: Create members table
-- Members represent gym members who can subscribe and use gym facilities

CREATE TABLE IF NOT EXISTS members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    branch_id UUID,
    member_number VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(50),
    medical_conditions TEXT,
    profile_photo_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    last_visit_date TIMESTAMP,
    settings JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_members_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_members_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_members_organization_id ON members(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_members_branch_id ON members(branch_id) WHERE NOT is_deleted;
CREATE INDEX idx_members_email ON members(email) WHERE NOT is_deleted;
CREATE INDEX idx_members_phone ON members(phone) WHERE NOT is_deleted;
CREATE INDEX idx_members_member_number ON members(member_number) WHERE NOT is_deleted;
CREATE INDEX idx_members_status ON members(status) WHERE NOT is_deleted;
CREATE INDEX idx_members_last_name ON members(last_name) WHERE NOT is_deleted;

-- Unique constraint on member_number within an organization
CREATE UNIQUE INDEX idx_members_org_member_number_unique ON members(organization_id, member_number) WHERE NOT is_deleted;

-- Unique constraint on email within an organization
CREATE UNIQUE INDEX idx_members_org_email_unique ON members(organization_id, email) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_members_updated_at BEFORE UPDATE ON members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE members IS 'Gym members who subscribe to membership plans';
COMMENT ON COLUMN members.member_number IS 'Unique member identification number within an organization';
COMMENT ON COLUMN members.medical_conditions IS 'Medical conditions or health notes';
COMMENT ON COLUMN members.last_visit_date IS 'Timestamp of the last gym visit (access log)';
