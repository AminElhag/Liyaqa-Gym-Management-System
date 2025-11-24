-- V16: Create additional indexes for performance optimization
-- This migration adds composite indexes and additional performance-optimized indexes

-- Organizations: Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_organizations_status_tier ON organizations(status, subscription_tier) WHERE NOT is_deleted;

-- Branches: Composite indexes for location-based queries
CREATE INDEX IF NOT EXISTS idx_branches_org_status ON branches(organization_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_branches_city_country ON branches(city, country) WHERE NOT is_deleted;

-- Members: Composite indexes for search and filtering
CREATE INDEX IF NOT EXISTS idx_members_org_status ON members(organization_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_members_org_branch ON members(organization_id, branch_id) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_members_name_search ON members(organization_id, last_name, first_name) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_members_registration_date ON members(organization_id, registration_date DESC) WHERE NOT is_deleted;

-- Membership Plans: Composite indexes for active plans
CREATE INDEX IF NOT EXISTS idx_membership_plans_org_active ON membership_plans(organization_id, is_active) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_membership_plans_branch_active ON membership_plans(branch_id, is_active) WHERE NOT is_deleted AND branch_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_membership_plans_price ON membership_plans(organization_id, price) WHERE NOT is_deleted AND is_active = TRUE;

-- Subscriptions: Composite indexes for subscription management
CREATE INDEX IF NOT EXISTS idx_subscriptions_member_status ON subscriptions(member_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_subscriptions_org_status ON subscriptions(organization_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_subscriptions_active_period ON subscriptions(member_id, start_date, end_date) WHERE NOT is_deleted AND status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_subscriptions_expiring ON subscriptions(organization_id, end_date) WHERE NOT is_deleted AND status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_subscriptions_auto_renew ON subscriptions(organization_id, next_billing_date) WHERE NOT is_deleted AND auto_renew = TRUE;

-- Classes: Composite indexes for class management
CREATE INDEX IF NOT EXISTS idx_classes_branch_active ON classes(branch_id, is_active) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_classes_org_type ON classes(organization_id, class_type) WHERE NOT is_deleted AND is_active = TRUE;

-- Class Schedules: Composite indexes for schedule queries
CREATE INDEX IF NOT EXISTS idx_class_schedules_class_date_status ON class_schedules(class_id, scheduled_date, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_class_schedules_org_date ON class_schedules(organization_id, scheduled_date) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_class_schedules_instructor_date ON class_schedules(instructor_id, scheduled_date) WHERE NOT is_deleted AND instructor_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_class_schedules_upcoming ON class_schedules(organization_id, scheduled_date, start_time) WHERE NOT is_deleted AND status = 'SCHEDULED';
CREATE INDEX IF NOT EXISTS idx_class_schedules_available ON class_schedules(organization_id, scheduled_date) WHERE NOT is_deleted AND status = 'SCHEDULED' AND current_bookings < max_capacity;

-- Bookings: Composite indexes for booking queries
CREATE INDEX IF NOT EXISTS idx_bookings_member_status ON bookings(member_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_bookings_schedule_status ON bookings(class_schedule_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_bookings_member_upcoming ON bookings(member_id, booking_date) WHERE NOT is_deleted AND status = 'CONFIRMED';
CREATE INDEX IF NOT EXISTS idx_bookings_org_date ON bookings(organization_id, booking_date DESC) WHERE NOT is_deleted;

-- Trainers: Composite indexes for trainer management
CREATE INDEX IF NOT EXISTS idx_trainers_org_status ON trainers(organization_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_trainers_branch_status ON trainers(branch_id, status) WHERE NOT is_deleted AND branch_id IS NOT NULL;

-- PT Sessions: Composite indexes for session management
CREATE INDEX IF NOT EXISTS idx_pt_sessions_trainer_date_status ON pt_sessions(trainer_id, scheduled_date, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_pt_sessions_member_date ON pt_sessions(member_id, scheduled_date DESC) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_pt_sessions_org_date ON pt_sessions(organization_id, scheduled_date) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_pt_sessions_upcoming ON pt_sessions(trainer_id, scheduled_date, start_time) WHERE NOT is_deleted AND status = 'SCHEDULED';

-- Payments: Composite indexes for payment queries
CREATE INDEX IF NOT EXISTS idx_payments_member_date ON payments(member_id, payment_date DESC) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_payments_org_date ON payments(organization_id, payment_date DESC) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_payments_org_status ON payments(organization_id, payment_status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_payments_reference ON payments(reference_type, reference_id) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_payments_pending ON payments(organization_id, payment_status) WHERE NOT is_deleted AND payment_status = 'PENDING';

-- Invoices: Composite indexes for invoice queries
CREATE INDEX IF NOT EXISTS idx_invoices_member_status ON invoices(member_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_invoices_org_status ON invoices(organization_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_invoices_org_due_date ON invoices(organization_id, due_date) WHERE NOT is_deleted AND status IN ('ISSUED', 'OVERDUE');
CREATE INDEX IF NOT EXISTS idx_invoices_overdue ON invoices(organization_id, due_date) WHERE NOT is_deleted AND status = 'ISSUED';

-- Access Logs: Composite indexes for access tracking
CREATE INDEX IF NOT EXISTS idx_access_logs_member_entry_time ON access_logs(member_id, entry_time DESC) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_access_logs_branch_entry_time ON access_logs(branch_id, entry_time DESC) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_access_logs_org_date ON access_logs(organization_id, CAST(entry_time AS date)) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_access_logs_status_date ON access_logs(branch_id, status, entry_time) WHERE NOT is_deleted;

-- Equipment: Composite indexes for equipment management
CREATE INDEX IF NOT EXISTS idx_equipment_branch_status ON equipment(branch_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_equipment_org_category ON equipment(organization_id, category) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_equipment_maintenance_due ON equipment(branch_id, next_maintenance_date) WHERE NOT is_deleted AND status = 'OPERATIONAL' AND next_maintenance_date IS NOT NULL;

-- Maintenance Tickets: Composite indexes for ticket management
CREATE INDEX IF NOT EXISTS idx_maintenance_tickets_equipment_status ON maintenance_tickets(equipment_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_maintenance_tickets_org_status ON maintenance_tickets(organization_id, status) WHERE NOT is_deleted;
CREATE INDEX IF NOT EXISTS idx_maintenance_tickets_assigned_status ON maintenance_tickets(assigned_to_id, status) WHERE NOT is_deleted AND assigned_to_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_maintenance_tickets_priority_status ON maintenance_tickets(organization_id, priority, status) WHERE NOT is_deleted AND status IN ('OPEN', 'IN_PROGRESS');

-- Add table statistics comments
COMMENT ON INDEX idx_subscriptions_expiring IS 'Optimizes queries for finding subscriptions expiring soon';
COMMENT ON INDEX idx_class_schedules_available IS 'Optimizes queries for finding available class slots';
COMMENT ON INDEX idx_invoices_overdue IS 'Optimizes queries for finding overdue invoices';
COMMENT ON INDEX idx_equipment_maintenance_due IS 'Optimizes queries for equipment needing maintenance';
