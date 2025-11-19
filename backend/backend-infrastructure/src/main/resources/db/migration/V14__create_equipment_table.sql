-- V14: Create equipment table
-- Equipment represents gym equipment and assets

CREATE TABLE IF NOT EXISTS equipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    equipment_number VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    manufacturer VARCHAR(255),
    model VARCHAR(255),
    serial_number VARCHAR(255),
    purchase_date DATE,
    purchase_price DECIMAL(10, 2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    warranty_expiry_date DATE,
    location VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'OPERATIONAL',
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    maintenance_frequency_days INTEGER,
    usage_instructions TEXT,
    image_url VARCHAR(500),
    notes TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_equipment_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_equipment_organization_id ON equipment(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_equipment_branch_id ON equipment(branch_id) WHERE NOT is_deleted;
CREATE INDEX idx_equipment_equipment_number ON equipment(equipment_number) WHERE NOT is_deleted;
CREATE INDEX idx_equipment_category ON equipment(category) WHERE NOT is_deleted;
CREATE INDEX idx_equipment_status ON equipment(status) WHERE NOT is_deleted;
CREATE INDEX idx_equipment_next_maintenance_date ON equipment(next_maintenance_date) WHERE NOT is_deleted;

-- Unique constraint on equipment_number within an organization
CREATE UNIQUE INDEX idx_equipment_org_number_unique ON equipment(organization_id, equipment_number) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_equipment_updated_at BEFORE UPDATE ON equipment
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE equipment IS 'Gym equipment and assets';
COMMENT ON COLUMN equipment.equipment_number IS 'Unique equipment identifier within an organization';
COMMENT ON COLUMN equipment.category IS 'Equipment category (CARDIO, STRENGTH, FREE_WEIGHTS, etc.)';
COMMENT ON COLUMN equipment.status IS 'Equipment status (OPERATIONAL, MAINTENANCE, OUT_OF_SERVICE, RETIRED)';
COMMENT ON COLUMN equipment.location IS 'Physical location within the gym (e.g., Floor 1, Zone A)';
COMMENT ON COLUMN equipment.maintenance_frequency_days IS 'Number of days between maintenance checks';
