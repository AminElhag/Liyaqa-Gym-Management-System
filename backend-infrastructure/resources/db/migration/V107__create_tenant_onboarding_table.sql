-- Create tenant_onboarding_progress table
CREATE TABLE tenant_onboarding_progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL,
  step VARCHAR(100) NOT NULL,
  status VARCHAR(50) NOT NULL,
  completed_at TIMESTAMP,
  data JSONB,

  CONSTRAINT fk_tenant FOREIGN KEY (tenant_id)
    REFERENCES tenants(id) ON DELETE CASCADE
);
