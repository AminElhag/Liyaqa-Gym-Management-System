-- V13: Create access_logs table
-- Access logs track member entry and exit from gym facilities

CREATE TABLE IF NOT EXISTS access_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    member_id UUID NOT NULL,
    access_point VARCHAR(100),
    entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP,
    access_method VARCHAR(50) NOT NULL,
    device_id VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'GRANTED',
    denial_reason VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_access_logs_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_access_logs_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_logs_member FOREIGN KEY (member_id)
        REFERENCES members(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_access_logs_organization_id ON access_logs(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_access_logs_branch_id ON access_logs(branch_id) WHERE NOT is_deleted;
CREATE INDEX idx_access_logs_member_id ON access_logs(member_id) WHERE NOT is_deleted;
CREATE INDEX idx_access_logs_entry_time ON access_logs(entry_time) WHERE NOT is_deleted;
CREATE INDEX idx_access_logs_status ON access_logs(status) WHERE NOT is_deleted;
CREATE INDEX idx_access_logs_access_method ON access_logs(access_method) WHERE NOT is_deleted;
CREATE INDEX idx_access_logs_device_id ON access_logs(device_id) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_access_logs_updated_at BEFORE UPDATE ON access_logs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE access_logs IS 'Member access logs for gym facility entry and exit';
COMMENT ON COLUMN access_logs.access_point IS 'Location or gate where access occurred';
COMMENT ON COLUMN access_logs.access_method IS 'Method of access (CARD, QR_CODE, BIOMETRIC, MANUAL)';
COMMENT ON COLUMN access_logs.status IS 'Access status (GRANTED, DENIED)';
COMMENT ON COLUMN access_logs.denial_reason IS 'Reason for access denial (if applicable)';
COMMENT ON COLUMN access_logs.device_id IS 'Identifier of the access control device';
