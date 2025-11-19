-- V6: Create classes table
-- Classes represent fitness class types offered by the gym (Yoga, Spinning, etc.)

CREATE TABLE IF NOT EXISTS classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    class_type VARCHAR(100) NOT NULL,
    difficulty_level VARCHAR(50),
    duration_minutes INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL,
    equipment_required JSONB,
    image_url VARCHAR(500),
    color_code VARCHAR(7),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_classes_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_classes_branch FOREIGN KEY (branch_id)
        REFERENCES branches(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_classes_organization_id ON classes(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_classes_branch_id ON classes(branch_id) WHERE NOT is_deleted;
CREATE INDEX idx_classes_class_type ON classes(class_type) WHERE NOT is_deleted;
CREATE INDEX idx_classes_is_active ON classes(is_active) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_classes_updated_at BEFORE UPDATE ON classes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE classes IS 'Fitness class types offered by the gym';
COMMENT ON COLUMN classes.class_type IS 'Type of class (YOGA, SPINNING, PILATES, ZUMBA, etc.)';
COMMENT ON COLUMN classes.difficulty_level IS 'Difficulty level (BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS)';
COMMENT ON COLUMN classes.duration_minutes IS 'Duration of the class in minutes';
COMMENT ON COLUMN classes.equipment_required IS 'JSON array of required equipment';
COMMENT ON COLUMN classes.color_code IS 'Hex color code for UI display';
