-- V11: Create payments table
-- Payments represent financial transactions for memberships, sessions, etc.

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    member_id UUID NOT NULL,
    payment_number VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    payment_gateway VARCHAR(50),
    payment_type VARCHAR(50) NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(50),
    description TEXT,
    metadata JSONB,
    processed_at TIMESTAMP,
    refund_amount DECIMAL(10, 2) DEFAULT 0.00,
    refund_date TIMESTAMP,
    refund_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_payments_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_member FOREIGN KEY (member_id)
        REFERENCES members(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_payments_organization_id ON payments(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_payments_member_id ON payments(member_id) WHERE NOT is_deleted;
CREATE INDEX idx_payments_payment_number ON payments(payment_number) WHERE NOT is_deleted;
CREATE INDEX idx_payments_payment_date ON payments(payment_date) WHERE NOT is_deleted;
CREATE INDEX idx_payments_payment_status ON payments(payment_status) WHERE NOT is_deleted;
CREATE INDEX idx_payments_payment_type ON payments(payment_type) WHERE NOT is_deleted;
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id) WHERE NOT is_deleted;
CREATE INDEX idx_payments_reference ON payments(reference_id, reference_type) WHERE NOT is_deleted;

-- Unique constraint on payment_number within an organization
CREATE UNIQUE INDEX idx_payments_org_number_unique ON payments(organization_id, payment_number) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE payments IS 'Financial transactions for memberships, sessions, and other services';
COMMENT ON COLUMN payments.payment_number IS 'Unique payment identifier within an organization';
COMMENT ON COLUMN payments.payment_method IS 'Payment method (CASH, CARD, BANK_TRANSFER, ONLINE, etc.)';
COMMENT ON COLUMN payments.payment_status IS 'Payment status (PENDING, COMPLETED, FAILED, REFUNDED)';
COMMENT ON COLUMN payments.payment_type IS 'Type of payment (MEMBERSHIP, PT_SESSION, CLASS, ENROLLMENT_FEE, etc.)';
COMMENT ON COLUMN payments.reference_id IS 'ID of the related entity (subscription, session, etc.)';
COMMENT ON COLUMN payments.reference_type IS 'Type of the related entity (SUBSCRIPTION, PT_SESSION, etc.)';
COMMENT ON COLUMN payments.metadata IS 'Additional payment metadata in JSON format';
