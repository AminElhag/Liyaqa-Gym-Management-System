-- V4: Create membership_plans table
-- Membership plans define the pricing and benefits for gym memberships

CREATE TABLE IF NOT EXISTS membership_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    branch_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    plan_type VARCHAR(50) NOT NULL,
    duration_days INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    enrollment_fee DECIMAL(10, 2) DEFAULT 0.00,
    access_level VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    features JSONB,
    max_class_bookings_per_month INTEGER,
    max_pt_sessions_per_month INTEGER,
    branch_access_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE',
    allowed_branch_ids JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_membership_plans_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_membership_plans_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_membership_plans_organization_id ON membership_plans(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_membership_plans_branch_id ON membership_plans(branch_id) WHERE NOT is_deleted;
CREATE INDEX idx_membership_plans_plan_type ON membership_plans(plan_type) WHERE NOT is_deleted;
CREATE INDEX idx_membership_plans_is_active ON membership_plans(is_active) WHERE NOT is_deleted;
CREATE INDEX idx_membership_plans_display_order ON membership_plans(display_order) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_membership_plans_updated_at BEFORE UPDATE ON membership_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE membership_plans IS 'Membership plans with pricing and benefits';
COMMENT ON COLUMN membership_plans.plan_type IS 'Type of plan (MONTHLY, QUARTERLY, ANNUAL, etc.)';
COMMENT ON COLUMN membership_plans.duration_days IS 'Duration of the plan in days';
COMMENT ON COLUMN membership_plans.access_level IS 'Access level (STANDARD, PREMIUM, VIP, etc.)';
COMMENT ON COLUMN membership_plans.features IS 'JSON array of plan features';
COMMENT ON COLUMN membership_plans.branch_access_type IS 'Branch access type (SINGLE, MULTIPLE, ALL)';
COMMENT ON COLUMN membership_plans.allowed_branch_ids IS 'JSON array of branch IDs for multi-branch access';
