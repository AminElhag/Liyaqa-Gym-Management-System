-- Create tenants table
CREATE TABLE tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  name_arabic VARCHAR(255),
  slug VARCHAR(100) NOT NULL UNIQUE,
  business_type VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  subscription_plan VARCHAR(50) NOT NULL,
  subscription_start_date DATE NOT NULL,
  subscription_end_date DATE NOT NULL,
  billing_cycle VARCHAR(50) NOT NULL,
  max_branches INT NOT NULL DEFAULT 1,
  max_members INT NOT NULL DEFAULT 500,
  max_staff INT NOT NULL DEFAULT 10,
  features JSONB NOT NULL DEFAULT '[]',
  contact_name VARCHAR(255) NOT NULL,
  contact_email VARCHAR(255) NOT NULL,
  contact_phone VARCHAR(50) NOT NULL,
  technical_contact_email VARCHAR(255),
  billing_contact_email VARCHAR(255),
  address_street VARCHAR(255),
  address_city VARCHAR(100),
  address_country VARCHAR(100),
  address_postal_code VARCHAR(20),
  vat_number VARCHAR(100),
  commercial_registration VARCHAR(100),
  logo VARCHAR(500),
  brand_colors JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by UUID NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

  CONSTRAINT fk_created_by FOREIGN KEY (created_by)
    REFERENCES platform_admins(id)
);

CREATE INDEX idx_tenants_slug ON tenants(slug) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenants_status ON tenants(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenants_subscription_end_date ON tenants(subscription_end_date);
