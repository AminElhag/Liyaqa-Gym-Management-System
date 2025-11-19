-- V1: Create organizations table
-- Organizations represent gym chains or independent gyms that use the system

CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    tax_id VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    website VARCHAR(255),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    subscription_tier VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    settings JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes for common queries
CREATE INDEX idx_organizations_email ON organizations(email) WHERE NOT is_deleted;
CREATE INDEX idx_organizations_status ON organizations(status) WHERE NOT is_deleted;
CREATE INDEX idx_organizations_created_at ON organizations(created_at) WHERE NOT is_deleted;

-- Add unique constraint on email for active organizations
CREATE UNIQUE INDEX idx_organizations_email_unique ON organizations(email) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_organizations_updated_at BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE organizations IS 'Organizations (gym chains or independent gyms) using the Liyaqa system';
COMMENT ON COLUMN organizations.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN organizations.version IS 'Version number for optimistic locking';
COMMENT ON COLUMN organizations.is_deleted IS 'Soft delete flag';
COMMENT ON COLUMN organizations.settings IS 'JSON configuration settings for the organization';
