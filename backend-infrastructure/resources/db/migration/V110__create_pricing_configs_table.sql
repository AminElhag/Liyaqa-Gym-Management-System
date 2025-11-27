-- Create pricing_configs table for usage-based pricing
CREATE TABLE pricing_configs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_active BOOLEAN NOT NULL DEFAULT false,

  -- SMS pricing configuration
  sms_free_quota INT NOT NULL DEFAULT 0,
  sms_price_per_unit DECIMAL(19,4) NOT NULL DEFAULT 0,
  sms_unit VARCHAR(50) NOT NULL DEFAULT 'message',
  sms_currency VARCHAR(3) NOT NULL DEFAULT 'SAR',

  -- Email pricing configuration
  email_free_quota INT NOT NULL DEFAULT 0,
  email_price_per_unit DECIMAL(19,4) NOT NULL DEFAULT 0,
  email_unit VARCHAR(50) NOT NULL DEFAULT 'email',
  email_currency VARCHAR(3) NOT NULL DEFAULT 'SAR',

  -- API pricing configuration
  api_free_quota INT NOT NULL DEFAULT 0,
  api_price_per_unit DECIMAL(19,4) NOT NULL DEFAULT 0,
  api_unit VARCHAR(50) NOT NULL DEFAULT 'call',
  api_currency VARCHAR(3) NOT NULL DEFAULT 'SAR',

  -- Storage pricing configuration
  storage_free_quota INT NOT NULL DEFAULT 0,
  storage_price_per_unit DECIMAL(19,4) NOT NULL DEFAULT 0,
  storage_unit VARCHAR(50) NOT NULL DEFAULT 'GB',
  storage_currency VARCHAR(3) NOT NULL DEFAULT 'SAR',

  effective_from TIMESTAMP NOT NULL,
  effective_until TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT chk_effective_dates CHECK (effective_until IS NULL OR effective_until >= effective_from),
  CONSTRAINT chk_sms_quota_positive CHECK (sms_free_quota >= 0),
  CONSTRAINT chk_email_quota_positive CHECK (email_free_quota >= 0),
  CONSTRAINT chk_api_quota_positive CHECK (api_free_quota >= 0),
  CONSTRAINT chk_storage_quota_positive CHECK (storage_free_quota >= 0)
);

-- Create indexes for efficient querying
CREATE INDEX idx_pricing_config_is_active ON pricing_configs(is_active);
CREATE INDEX idx_pricing_config_effective_from ON pricing_configs(effective_from);
CREATE INDEX idx_pricing_config_effective_until ON pricing_configs(effective_until);

-- Add comment to table
COMMENT ON TABLE pricing_configs IS 'Usage-based pricing configuration for add-ons (SMS, Email, API, Storage)';

-- Add comments to key columns
COMMENT ON COLUMN pricing_configs.is_active IS 'Whether this pricing configuration is currently active';
COMMENT ON COLUMN pricing_configs.sms_free_quota IS 'Number of free SMS messages per billing period';
COMMENT ON COLUMN pricing_configs.sms_price_per_unit IS 'Price per SMS message after free quota';
COMMENT ON COLUMN pricing_configs.email_free_quota IS 'Number of free emails per billing period';
COMMENT ON COLUMN pricing_configs.email_price_per_unit IS 'Price per email after free quota';
COMMENT ON COLUMN pricing_configs.api_free_quota IS 'Number of free API calls per billing period';
COMMENT ON COLUMN pricing_configs.api_price_per_unit IS 'Price per API call after free quota';
COMMENT ON COLUMN pricing_configs.storage_free_quota IS 'Free storage quota in GB per billing period';
COMMENT ON COLUMN pricing_configs.storage_price_per_unit IS 'Price per GB of storage after free quota';
COMMENT ON COLUMN pricing_configs.effective_from IS 'When this pricing configuration becomes effective';
COMMENT ON COLUMN pricing_configs.effective_until IS 'When this pricing configuration expires (NULL for no expiration)';

-- Insert default pricing configuration
INSERT INTO pricing_configs (
  name,
  description,
  is_active,
  sms_free_quota,
  sms_price_per_unit,
  sms_unit,
  sms_currency,
  email_free_quota,
  email_price_per_unit,
  email_unit,
  email_currency,
  api_free_quota,
  api_price_per_unit,
  api_unit,
  api_currency,
  storage_free_quota,
  storage_price_per_unit,
  storage_unit,
  storage_currency,
  effective_from
) VALUES (
  'Default Usage-Based Pricing',
  'Standard pricing for usage-based add-ons',
  true,
  100,      -- 100 free SMS per month
  0.10,     -- SAR 0.10 per SMS after free quota
  'message',
  'SAR',
  1000,     -- 1000 free emails per month
  0.01,     -- SAR 0.01 per email after free quota
  'email',
  'SAR',
  10000,    -- 10,000 free API calls per month
  0.001,    -- SAR 0.001 per API call after free quota
  'call',
  'SAR',
  0,        -- No free storage quota
  0.50,     -- SAR 0.50 per GB of storage
  'GB',
  'SAR',
  CURRENT_TIMESTAMP
);
