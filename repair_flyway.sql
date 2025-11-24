-- Script to repair Flyway schema history
-- This removes the failed V16 migration so it can be re-executed with the fixed version

-- Delete the failed migration entry for V16
DELETE FROM flyway_schema_history WHERE version = '16' AND success = false;

-- Verify the deletion
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
