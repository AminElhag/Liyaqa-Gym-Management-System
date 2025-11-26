-- Create platform_invoices table
CREATE TABLE platform_invoices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL,
  invoice_number VARCHAR(100) NOT NULL UNIQUE,
  amount_value DECIMAL(10,2) NOT NULL,
  amount_currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
  vat_amount_value DECIMAL(10,2) NOT NULL,
  total_amount_value DECIMAL(10,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  due_date DATE NOT NULL,
  paid_at TIMESTAMP,
  zatca_clearance_id VARCHAR(255),
  qr_code_data TEXT,
  items JSONB NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_tenant FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
);

CREATE INDEX idx_platform_inv_tenant ON platform_invoices(tenant_id);
CREATE INDEX idx_platform_inv_status ON platform_invoices(status);
CREATE INDEX idx_platform_inv_due_date ON platform_invoices(due_date);
