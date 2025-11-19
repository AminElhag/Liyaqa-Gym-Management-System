-- V2: Create branches table
-- Branches represent physical gym locations belonging to an organization

CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    opening_hours JSONB,
    facilities JSONB,
    capacity INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    settings JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_branches_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_branches_organization_id ON branches(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_branches_status ON branches(status) WHERE NOT is_deleted;
CREATE INDEX idx_branches_code ON branches(code) WHERE NOT is_deleted;
CREATE INDEX idx_branches_location ON branches(latitude, longitude) WHERE NOT is_deleted;

-- Unique constraint on branch code within an organization
CREATE UNIQUE INDEX idx_branches_org_code_unique ON branches(organization_id, code) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_branches_updated_at BEFORE UPDATE ON branches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE branches IS 'Physical gym locations belonging to organizations';
COMMENT ON COLUMN branches.code IS 'Unique code for the branch within an organization';
COMMENT ON COLUMN branches.opening_hours IS 'JSON structure defining operating hours';
COMMENT ON COLUMN branches.facilities IS 'JSON array of available facilities (pool, sauna, etc.)';
