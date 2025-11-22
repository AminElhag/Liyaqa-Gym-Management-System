-- V17: Create users table with initial master admin account
-- Users represent authenticated accounts that can access the system

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    branch_id UUID REFERENCES branches(id),
    member_id UUID REFERENCES members(id),
    staff_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_role CHECK (role IN ('MEMBER', 'TRAINER', 'STAFF', 'ADMIN'))
);

-- Create indexes for common queries
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_organization_id ON users(organization_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Add unique constraint on email per organization
CREATE UNIQUE INDEX idx_users_email_org_unique ON users(email, organization_id) WHERE is_active;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE users IS 'User accounts for authentication and authorization';
COMMENT ON COLUMN users.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN users.email IS 'User email address (username for login)';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN users.role IS 'User role: MEMBER, TRAINER, STAFF, or ADMIN';
COMMENT ON COLUMN users.organization_id IS 'Organization this user belongs to';
COMMENT ON COLUMN users.branch_id IS 'Branch this user belongs to (NULL for org-level admins)';
COMMENT ON COLUMN users.member_id IS 'Reference to member record (for MEMBER role)';
COMMENT ON COLUMN users.staff_id IS 'Reference to staff record (for STAFF/TRAINER roles)';
COMMENT ON COLUMN users.must_change_password IS 'Force password change on next login';

-- Insert default organization if it doesn't exist
INSERT INTO organizations (id, name, email, country, status, subscription_tier)
VALUES (
    '00000000-0000-0000-0000-000000000001'::UUID,
    'Liyaqa Gym Management',
    'admin@liyaqa.com',
    'Saudi Arabia',
    'ACTIVE',
    'PREMIUM'
) ON CONFLICT (id) DO NOTHING;

-- Insert master admin account
-- Username: admin@liyaqa.com
-- Password: 1234
-- BCrypt hash with strength 12: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96mXPtG2.8OmG5G3VJq
INSERT INTO users (id, email, password_hash, role, organization_id, branch_id, member_id, staff_id, is_active, is_email_verified, must_change_password, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000002'::UUID,
    'admin@liyaqa.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96mXPtG2.8OmG5G3VJq',
    'ADMIN',
    '00000000-0000-0000-0000-000000000001'::UUID,
    NULL,
    NULL,
    NULL,
    TRUE,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;
