-- Add custom domain support to tenants table
ALTER TABLE tenants
ADD COLUMN custom_domain VARCHAR(255),
ADD COLUMN domain_verification_token VARCHAR(255),
ADD COLUMN domain_verification_status VARCHAR(50),
ADD COLUMN domain_verified_at TIMESTAMP,
ADD COLUMN ssl_certificate_id VARCHAR(255);

-- Add unique constraint on custom_domain
CREATE UNIQUE INDEX idx_tenants_custom_domain ON tenants(custom_domain)
WHERE custom_domain IS NOT NULL AND is_deleted = FALSE;

-- Add index for domain verification status
CREATE INDEX idx_tenants_domain_status ON tenants(domain_verification_status)
WHERE domain_verification_status IS NOT NULL AND is_deleted = FALSE;

-- Add comments for documentation
COMMENT ON COLUMN tenants.custom_domain IS 'Custom domain configured by tenant (e.g., gym.example.com)';
COMMENT ON COLUMN tenants.domain_verification_token IS 'Token used for DNS TXT record verification';
COMMENT ON COLUMN tenants.domain_verification_status IS 'Status: PENDING, VERIFIED, FAILED';
COMMENT ON COLUMN tenants.domain_verified_at IS 'Timestamp when domain was successfully verified';
COMMENT ON COLUMN tenants.ssl_certificate_id IS 'ID of the SSL certificate for custom domain';
