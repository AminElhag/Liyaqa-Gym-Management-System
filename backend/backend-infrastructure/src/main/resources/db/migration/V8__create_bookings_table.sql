-- V8: Create bookings table
-- Bookings represent member reservations for class schedules

CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    class_schedule_id UUID NOT NULL,
    member_id UUID NOT NULL,
    booking_number VARCHAR(50) NOT NULL,
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    check_in_time TIMESTAMP,
    cancellation_time TIMESTAMP,
    cancellation_reason TEXT,
    no_show BOOLEAN NOT NULL DEFAULT FALSE,
    waitlist BOOLEAN NOT NULL DEFAULT FALSE,
    waitlist_position INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_bookings_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_class_schedule FOREIGN KEY (class_schedule_id)
        REFERENCES class_schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_member FOREIGN KEY (member_id)
        REFERENCES members(id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_bookings_organization_id ON bookings(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_bookings_class_schedule_id ON bookings(class_schedule_id) WHERE NOT is_deleted;
CREATE INDEX idx_bookings_member_id ON bookings(member_id) WHERE NOT is_deleted;
CREATE INDEX idx_bookings_status ON bookings(status) WHERE NOT is_deleted;
CREATE INDEX idx_bookings_booking_date ON bookings(booking_date) WHERE NOT is_deleted;
CREATE INDEX idx_bookings_booking_number ON bookings(booking_number) WHERE NOT is_deleted;
CREATE INDEX idx_bookings_waitlist ON bookings(waitlist) WHERE NOT is_deleted AND waitlist = TRUE;

-- Unique constraint: a member can only book a class schedule once
CREATE UNIQUE INDEX idx_bookings_member_schedule_unique ON bookings(member_id, class_schedule_id) WHERE NOT is_deleted;

-- Unique constraint on booking_number within an organization
CREATE UNIQUE INDEX idx_bookings_org_number_unique ON bookings(organization_id, booking_number) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE bookings IS 'Member bookings for fitness class schedules';
COMMENT ON COLUMN bookings.booking_number IS 'Unique booking identifier within an organization';
COMMENT ON COLUMN bookings.status IS 'Booking status (CONFIRMED, CANCELLED, COMPLETED)';
COMMENT ON COLUMN bookings.no_show IS 'Whether the member was a no-show for the booking';
COMMENT ON COLUMN bookings.waitlist IS 'Whether this is a waitlist booking';
COMMENT ON COLUMN bookings.waitlist_position IS 'Position in the waitlist (if applicable)';
