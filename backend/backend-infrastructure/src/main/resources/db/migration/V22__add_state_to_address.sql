-- Migration: Add state column to branches table for addresses
-- Description: Adds state/province field to address embeddable in branches table

-- Add state column to branches table with default value
ALTER TABLE branches ADD COLUMN state VARCHAR(100) NOT NULL DEFAULT 'Riyadh Region';
