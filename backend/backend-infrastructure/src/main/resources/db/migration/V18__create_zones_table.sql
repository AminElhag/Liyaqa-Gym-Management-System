-- V18: Create zones table
-- Zones represent restricted areas within a branch (e.g., Cardio Zone, Pool, VIP Area)

CREATE TABLE IF NOT EXISTS zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    required_features JSONB NOT NULL DEFAULT '[]'::jsonb,
    capacity INTEGER,
    gender_restriction VARCHAR(10),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_zones_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE CASCADE,
    CONSTRAINT chk_zones_capacity CHECK (capacity IS NULL OR capacity > 0),
    CONSTRAINT chk_zones_gender_restriction CHECK (gender_restriction IS NULL OR gender_restriction IN ('MALE', 'FEMALE'))
);

-- Create indexes
CREATE INDEX idx_zone_branch_id ON zones(branch_id);
CREATE INDEX idx_zone_name ON zones(name);
CREATE INDEX idx_zone_is_active ON zones(is_active);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_zones_updated_at BEFORE UPDATE ON zones
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE zones IS 'Restricted areas within branches requiring specific access permissions';
COMMENT ON COLUMN zones.required_features IS 'JSON array of features required in membership plan for access';
COMMENT ON COLUMN zones.capacity IS 'Maximum number of people allowed in the zone simultaneously';
COMMENT ON COLUMN zones.gender_restriction IS 'Gender restriction for the zone (MALE, FEMALE, or NULL for no restriction)';
