# Flyway Migration Fix - V16 Index Creation

## Problem
The V16 migration (`V16__create_indexes.sql`) failed because some indexes already existed in the database. This caused the application to fail to start with the error:
```
ERROR: relation "idx_organizations_status_tier" already exists
```

## Solution Applied
Modified `/backend/backend-infrastructure/src/main/resources/db/migration/V16__create_indexes.sql` to add `IF NOT EXISTS` to all `CREATE INDEX` statements, making the migration idempotent.

## Steps to Complete the Fix

### Option 1: Using psql (Recommended)
1. Connect to your database:
   ```bash
   psql -h localhost -p 5434 -U liyaqa_admin -d liyaqa_gym
   ```

2. Delete the failed migration entry:
   ```sql
   DELETE FROM flyway_schema_history WHERE version = '16' AND success = false;
   ```

3. Verify the deletion:
   ```sql
   SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
   ```

4. Exit psql:
   ```
   \q
   ```

### Option 2: Using the repair script
Run the provided SQL script:
```bash
PGPASSWORD=liyaqa_password psql -h localhost -p 5434 -U liyaqa_admin -d liyaqa_gym -f repair_flyway.sql
```

### Option 3: Using Flyway Command Line
If you have Flyway CLI installed:
```bash
flyway repair
```

## After Repair
1. Rebuild the project:
   ```bash
   ./gradlew clean build -x test
   ```

2. Restart the application:
   ```bash
   ./gradlew bootRun
   ```

The migration should now run successfully, skipping indexes that already exist due to the `IF NOT EXISTS` clause.

## What Changed
All 48 `CREATE INDEX` statements in V16__create_indexes.sql now include `IF NOT EXISTS`:
- Before: `CREATE INDEX idx_name ON table_name(...)`
- After: `CREATE INDEX IF NOT EXISTS idx_name ON table_name(...)`

This makes the migration safe to re-run even if some or all indexes already exist.
