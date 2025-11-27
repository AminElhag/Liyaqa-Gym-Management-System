-- V21: Add tenant_id columns to all tables for multi-tenant data isolation
-- This migration adds tenant_id to tables that need it for proper tenant scoping

-- Add tenant_id to members table
ALTER TABLE members
ADD COLUMN tenant_id UUID;

-- Add tenant_id to trainers table
ALTER TABLE trainers
ADD COLUMN tenant_id UUID;

-- Add tenant_id to subscriptions table
ALTER TABLE subscriptions
ADD COLUMN tenant_id UUID;

-- Add tenant_id to payments table
ALTER TABLE payments
ADD COLUMN tenant_id UUID;

-- Add tenant_id to invoices table
ALTER TABLE invoices
ADD COLUMN tenant_id UUID;

-- Update tenant_id values based on organization relationships
-- For members: get tenant_id from branches -> organizations (organization.id IS the tenant_id)
UPDATE members m
SET tenant_id = o.id
FROM branches b
JOIN organizations o ON b.organization_id = o.id
WHERE m.branch_id = b.id;

-- For trainers: get tenant_id from branches -> organizations (organization.id IS the tenant_id)
UPDATE trainers t
SET tenant_id = o.id
FROM branches b
JOIN organizations o ON b.organization_id = o.id
WHERE t.branch_id = b.id;

-- For subscriptions: get tenant_id from members
UPDATE subscriptions s
SET tenant_id = m.tenant_id
FROM members m
WHERE s.member_id = m.id;

-- For payments: get tenant_id from organization (organization.id IS the tenant_id)
UPDATE payments p
SET tenant_id = p.organization_id;

-- For invoices: get tenant_id from organization (organization.id IS the tenant_id)
UPDATE invoices i
SET tenant_id = i.organization_id;

-- Make tenant_id NOT NULL after populating values
ALTER TABLE members ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE trainers ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE subscriptions ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE payments ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE invoices ALTER COLUMN tenant_id SET NOT NULL;

-- Create indexes for tenant_id columns for better query performance
CREATE INDEX idx_members_tenant_id ON members(tenant_id);
CREATE INDEX idx_trainers_tenant_id ON trainers(tenant_id);
CREATE INDEX idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX idx_payments_tenant_id ON payments(tenant_id);
CREATE INDEX idx_invoices_tenant_id ON invoices(tenant_id);

-- Add composite indexes for common queries
CREATE INDEX idx_members_tenant_branch ON members(tenant_id, branch_id);
CREATE INDEX idx_trainers_tenant_branch ON trainers(tenant_id, branch_id);
CREATE INDEX idx_subscriptions_tenant_member ON subscriptions(tenant_id, member_id);