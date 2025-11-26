-- Create tenant_usage_metrics table
CREATE TABLE tenant_usage_metrics (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL,
  period_year INT NOT NULL,
  period_month INT NOT NULL,
  total_members INT NOT NULL DEFAULT 0,
  active_members INT NOT NULL DEFAULT 0,
  total_branches INT NOT NULL DEFAULT 0,
  total_staff INT NOT NULL DEFAULT 0,
  total_bookings INT NOT NULL DEFAULT 0,
  total_revenue_value DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_revenue_currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
  storage_used_mb BIGINT NOT NULL DEFAULT 0,
  api_calls_count BIGINT NOT NULL DEFAULT 0,
  sms_messages_sent INT NOT NULL DEFAULT 0,
  emails_sent INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_tenant FOREIGN KEY (tenant_id)
    REFERENCES tenants(id),
  CONSTRAINT uk_tenant_period UNIQUE (tenant_id, period_year, period_month)
);
