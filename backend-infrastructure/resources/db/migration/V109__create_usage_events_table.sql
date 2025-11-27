-- Create usage_events table for tracking billable usage
CREATE TABLE usage_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  quantity INT NOT NULL,
  metadata JSONB NOT NULL DEFAULT '{}',
  occurred_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_usage_event_tenant FOREIGN KEY (tenant_id)
    REFERENCES tenants(id) ON DELETE CASCADE,
  CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

-- Create indexes for efficient querying
CREATE INDEX idx_usage_event_tenant_id ON usage_events(tenant_id);
CREATE INDEX idx_usage_event_event_type ON usage_events(event_type);
CREATE INDEX idx_usage_event_occurred_at ON usage_events(occurred_at);
CREATE INDEX idx_usage_event_tenant_type ON usage_events(tenant_id, event_type);
CREATE INDEX idx_usage_event_tenant_occurred ON usage_events(tenant_id, occurred_at);

-- Add comment to table
COMMENT ON TABLE usage_events IS 'Tracks billable usage events for usage-based billing (API calls, SMS, emails, storage)';

-- Add comments to columns
COMMENT ON COLUMN usage_events.event_type IS 'Type of usage event: API_CALL, SMS_MESSAGE, EMAIL_SENT, STORAGE_USAGE';
COMMENT ON COLUMN usage_events.quantity IS 'Quantity of the usage event (e.g., number of SMS messages, MB of storage)';
COMMENT ON COLUMN usage_events.metadata IS 'Additional metadata about the event (e.g., API endpoint, recipient count)';
COMMENT ON COLUMN usage_events.occurred_at IS 'When the usage event occurred (used for billing period calculation)';
