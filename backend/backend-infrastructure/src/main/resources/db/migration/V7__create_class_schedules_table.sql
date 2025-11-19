-- V7: Create class_schedules table
-- Class schedules represent specific instances of classes at particular times

CREATE TABLE IF NOT EXISTS class_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    class_id UUID NOT NULL,
    instructor_id UUID,
    room_name VARCHAR(100),
    scheduled_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_capacity INTEGER NOT NULL,
    current_bookings INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    cancellation_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_class_schedules_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_class_schedules_class FOREIGN KEY (class_id)
        REFERENCES classes(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_class_schedules_organization_id ON class_schedules(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_class_schedules_class_id ON class_schedules(class_id) WHERE NOT is_deleted;
CREATE INDEX idx_class_schedules_instructor_id ON class_schedules(instructor_id) WHERE NOT is_deleted;
CREATE INDEX idx_class_schedules_scheduled_date ON class_schedules(scheduled_date) WHERE NOT is_deleted;
CREATE INDEX idx_class_schedules_status ON class_schedules(status) WHERE NOT is_deleted;
CREATE INDEX idx_class_schedules_date_time ON class_schedules(scheduled_date, start_time) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_class_schedules_updated_at BEFORE UPDATE ON class_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE class_schedules IS 'Scheduled instances of fitness classes';
COMMENT ON COLUMN class_schedules.status IS 'Schedule status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)';
COMMENT ON COLUMN class_schedules.current_bookings IS 'Current number of bookings for this schedule';
COMMENT ON COLUMN class_schedules.room_name IS 'Name or identifier of the room where class takes place';
