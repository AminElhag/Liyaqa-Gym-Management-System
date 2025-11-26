-- Create tenant_subscriptions table
CREATE TABLE tenant_subscriptions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL,
  plan VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  amount_value DECIMAL(10,2) NOT NULL,
  amount_currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
  billing_cycle VARCHAR(50) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
  payment_method VARCHAR(100),
  next_billing_date DATE NOT NULL,
  trial_ends_at DATE,
  cancelled_at TIMESTAMP,
  cancellation_reason TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_tenant FOREIGN KEY (tenant_id)
    REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_tenant_subs_next_billing ON tenant_subscriptions(next_billing_date);
CREATE INDEX idx_tenant_subs_status ON tenant_subscriptions(status);
