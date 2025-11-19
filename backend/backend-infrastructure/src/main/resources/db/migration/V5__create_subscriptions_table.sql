-- V5: Create subscriptions table
-- Subscriptions link members to membership plans and track their active memberships

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    member_id UUID NOT NULL,
    membership_plan_id UUID NOT NULL,
    subscription_number VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    payment_method VARCHAR(50),
    amount_paid DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_frequency VARCHAR(50) NOT NULL DEFAULT 'ONE_TIME',
    next_billing_date DATE,
    cancellation_date DATE,
    cancellation_reason TEXT,
    notes TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_subscriptions_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_subscriptions_member FOREIGN KEY (member_id)
        REFERENCES members(id) ON DELETE RESTRICT,
    CONSTRAINT fk_subscriptions_membership_plan FOREIGN KEY (membership_plan_id)
        REFERENCES membership_plans(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_subscriptions_organization_id ON subscriptions(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_subscriptions_member_id ON subscriptions(member_id) WHERE NOT is_deleted;
CREATE INDEX idx_subscriptions_membership_plan_id ON subscriptions(membership_plan_id) WHERE NOT is_deleted;
CREATE INDEX idx_subscriptions_status ON subscriptions(status) WHERE NOT is_deleted;
CREATE INDEX idx_subscriptions_start_date ON subscriptions(start_date) WHERE NOT is_deleted;
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date) WHERE NOT is_deleted;
CREATE INDEX idx_subscriptions_next_billing_date ON subscriptions(next_billing_date) WHERE NOT is_deleted;
CREATE INDEX idx_subscriptions_subscription_number ON subscriptions(subscription_number) WHERE NOT is_deleted;

-- Unique constraint on subscription_number within an organization
CREATE UNIQUE INDEX idx_subscriptions_org_number_unique ON subscriptions(organization_id, subscription_number) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE subscriptions IS 'Active and historical member subscriptions';
COMMENT ON COLUMN subscriptions.subscription_number IS 'Unique subscription identifier within an organization';
COMMENT ON COLUMN subscriptions.status IS 'Subscription status (ACTIVE, SUSPENDED, EXPIRED, CANCELLED)';
COMMENT ON COLUMN subscriptions.auto_renew IS 'Whether subscription auto-renews on expiration';
COMMENT ON COLUMN subscriptions.payment_frequency IS 'Payment frequency (ONE_TIME, MONTHLY, QUARTERLY, ANNUAL)';
COMMENT ON COLUMN subscriptions.metadata IS 'Additional metadata in JSON format';
