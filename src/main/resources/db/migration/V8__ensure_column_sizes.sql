-- ===================================================================================
-- V8: Ensure Column Sizes (idempotent fix)
-- This migration ensures all column sizes are correct
-- STATUS needs VARCHAR(50) to support PASSWORD_CHANGE_REQUIRED (24 chars)
-- ===================================================================================

-- Unconditionally set status column to VARCHAR(50)
-- This is safe as VARCHAR can be extended without data loss
ALTER TABLE users ALTER COLUMN status TYPE VARCHAR(50);

-- Ensure phone_number is adequate size
ALTER TABLE users ALTER COLUMN phone_number TYPE VARCHAR(20);
