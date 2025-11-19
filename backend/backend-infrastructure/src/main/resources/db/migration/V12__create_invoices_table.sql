-- V12: Create invoices table
-- Invoices represent billing documents for members

CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    member_id UUID NOT NULL,
    invoice_number VARCHAR(50) NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    amount_due DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    payment_terms TEXT,
    notes TEXT,
    line_items JSONB NOT NULL,
    metadata JSONB,
    issued_at TIMESTAMP,
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_invoices_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoices_member FOREIGN KEY (member_id)
        REFERENCES members(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_invoices_organization_id ON invoices(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_invoices_member_id ON invoices(member_id) WHERE NOT is_deleted;
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number) WHERE NOT is_deleted;
CREATE INDEX idx_invoices_invoice_date ON invoices(invoice_date) WHERE NOT is_deleted;
CREATE INDEX idx_invoices_due_date ON invoices(due_date) WHERE NOT is_deleted;
CREATE INDEX idx_invoices_status ON invoices(status) WHERE NOT is_deleted;

-- Unique constraint on invoice_number within an organization
CREATE UNIQUE INDEX idx_invoices_org_number_unique ON invoices(organization_id, invoice_number) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_invoices_updated_at BEFORE UPDATE ON invoices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE invoices IS 'Billing invoices for members';
COMMENT ON COLUMN invoices.invoice_number IS 'Unique invoice identifier within an organization';
COMMENT ON COLUMN invoices.status IS 'Invoice status (DRAFT, ISSUED, PAID, OVERDUE, CANCELLED)';
COMMENT ON COLUMN invoices.line_items IS 'JSON array of invoice line items with description, quantity, price';
COMMENT ON COLUMN invoices.amount_due IS 'Remaining amount to be paid (total_amount - amount_paid)';
