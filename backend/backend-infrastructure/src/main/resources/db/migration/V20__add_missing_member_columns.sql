-- V20: Add missing columns to members table to match JPA entity
-- Add national_id and notes columns

-- Add national_id column
ALTER TABLE members ADD COLUMN IF NOT EXISTS national_id VARCHAR(50);

-- Add notes column
ALTER TABLE members ADD COLUMN IF NOT EXISTS notes TEXT;

-- Add index on national_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_members_national_id ON members(national_id) WHERE NOT is_deleted;

-- Add comments
COMMENT ON COLUMN members.national_id IS 'National ID or passport number of the member';
COMMENT ON COLUMN members.notes IS 'Additional notes about the member';
