-- Add tenant_id to all existing tables for multi-tenancy support

-- Add tenant_id columns
ALTER TABLE organizations ADD COLUMN tenant_id UUID;
ALTER TABLE branches ADD COLUMN tenant_id UUID;
ALTER TABLE members ADD COLUMN tenant_id UUID;
ALTER TABLE subscriptions ADD COLUMN tenant_id UUID;
ALTER TABLE classes ADD COLUMN tenant_id UUID;
ALTER TABLE class_schedules ADD COLUMN tenant_id UUID;
ALTER TABLE bookings ADD COLUMN tenant_id UUID;
ALTER TABLE trainers ADD COLUMN tenant_id UUID;
ALTER TABLE payments ADD COLUMN tenant_id UUID;
ALTER TABLE invoices ADD COLUMN tenant_id UUID;
ALTER TABLE equipment ADD COLUMN tenant_id UUID;

-- Add foreign key constraints
ALTER TABLE organizations
  ADD CONSTRAINT fk_organizations_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE branches
  ADD CONSTRAINT fk_branches_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE members
  ADD CONSTRAINT fk_members_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE subscriptions
  ADD CONSTRAINT fk_subscriptions_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE classes
  ADD CONSTRAINT fk_classes_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE class_schedules
  ADD CONSTRAINT fk_class_schedules_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE bookings
  ADD CONSTRAINT fk_bookings_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE trainers
  ADD CONSTRAINT fk_trainers_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE payments
  ADD CONSTRAINT fk_payments_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE invoices
  ADD CONSTRAINT fk_invoices_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE equipment
  ADD CONSTRAINT fk_equipment_tenant
  FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Add indexes for better query performance
CREATE INDEX idx_organizations_tenant ON organizations(tenant_id);
CREATE INDEX idx_branches_tenant ON branches(tenant_id);
CREATE INDEX idx_members_tenant ON members(tenant_id);
CREATE INDEX idx_subscriptions_tenant ON subscriptions(tenant_id);
CREATE INDEX idx_classes_tenant ON classes(tenant_id);
CREATE INDEX idx_class_schedules_tenant ON class_schedules(tenant_id);
CREATE INDEX idx_bookings_tenant ON bookings(tenant_id);
CREATE INDEX idx_trainers_tenant ON trainers(tenant_id);
CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_invoices_tenant ON invoices(tenant_id);
CREATE INDEX idx_equipment_tenant ON equipment(tenant_id);
