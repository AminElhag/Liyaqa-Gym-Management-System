-- V15: Create maintenance_tickets table
-- Maintenance tickets track equipment repairs and maintenance tasks

CREATE TABLE IF NOT EXISTS maintenance_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    equipment_id UUID NOT NULL,
    ticket_number VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    ticket_type VARCHAR(50) NOT NULL DEFAULT 'MAINTENANCE',
    reported_by_id UUID,
    assigned_to_id UUID,
    reported_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_date TIMESTAMP,
    started_date TIMESTAMP,
    completed_date TIMESTAMP,
    resolution_notes TEXT,
    estimated_cost DECIMAL(10, 2),
    actual_cost DECIMAL(10, 2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    parts_replaced JSONB,
    attachments JSONB,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_maintenance_tickets_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_maintenance_tickets_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_maintenance_tickets_organization_id ON maintenance_tickets(organization_id) WHERE NOT is_deleted;
CREATE INDEX idx_maintenance_tickets_equipment_id ON maintenance_tickets(equipment_id) WHERE NOT is_deleted;
CREATE INDEX idx_maintenance_tickets_ticket_number ON maintenance_tickets(ticket_number) WHERE NOT is_deleted;
CREATE INDEX idx_maintenance_tickets_status ON maintenance_tickets(status) WHERE NOT is_deleted;
CREATE INDEX idx_maintenance_tickets_priority ON maintenance_tickets(priority) WHERE NOT is_deleted;
CREATE INDEX idx_maintenance_tickets_ticket_type ON maintenance_tickets(ticket_type) WHERE NOT is_deleted;
CREATE INDEX idx_maintenance_tickets_assigned_to ON maintenance_tickets(assigned_to_id) WHERE NOT is_deleted;
CREATE INDEX idx_maintenance_tickets_reported_date ON maintenance_tickets(reported_date) WHERE NOT is_deleted;

-- Unique constraint on ticket_number within an organization
CREATE UNIQUE INDEX idx_maintenance_tickets_org_number_unique ON maintenance_tickets(organization_id, ticket_number) WHERE NOT is_deleted;

-- Create trigger to update updated_at timestamp
CREATE TRIGGER update_maintenance_tickets_updated_at BEFORE UPDATE ON maintenance_tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE maintenance_tickets IS 'Equipment maintenance and repair tickets';
COMMENT ON COLUMN maintenance_tickets.ticket_number IS 'Unique ticket identifier within an organization';
COMMENT ON COLUMN maintenance_tickets.priority IS 'Ticket priority (LOW, MEDIUM, HIGH, URGENT)';
COMMENT ON COLUMN maintenance_tickets.status IS 'Ticket status (OPEN, IN_PROGRESS, COMPLETED, CANCELLED)';
COMMENT ON COLUMN maintenance_tickets.ticket_type IS 'Type of ticket (MAINTENANCE, REPAIR, INSPECTION, REPLACEMENT)';
COMMENT ON COLUMN maintenance_tickets.parts_replaced IS 'JSON array of parts replaced during maintenance';
COMMENT ON COLUMN maintenance_tickets.attachments IS 'JSON array of attachment URLs (photos, documents)';
