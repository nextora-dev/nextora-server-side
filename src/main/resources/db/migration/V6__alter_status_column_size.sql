-- ===================================================================================
-- V6: Alter status column size
-- Fixes status column to accommodate longer status values like PASSWORD_CHANGE_REQUIRED
-- ===================================================================================

-- Alter the status column to VARCHAR(50) to support all status enum values
ALTER TABLE users ALTER COLUMN status TYPE VARCHAR(50);

-- Also ensure phone_number column is adequate
ALTER TABLE users ALTER COLUMN phone_number TYPE VARCHAR(20);
