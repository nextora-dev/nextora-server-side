-- ===================================================================================
-- V11: Update status check constraint
-- Updates the users_status_check constraint to use DEACTIVATED instead of DEACTIVATE
-- ===================================================================================

-- Drop the existing check constraint if it exists
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_status_check;

-- Add the updated check constraint with correct status values
ALTER TABLE users ADD CONSTRAINT users_status_check
    CHECK (status IN ('ACTIVE', 'DEACTIVATED', 'SUSPENDED', 'DELETED', 'PASSWORD_CHANGE_REQUIRED', 'PENDING_VERIFICATION'));

-- Update any existing records with DEACTIVATE to DEACTIVATED
UPDATE users SET status = 'DEACTIVATED' WHERE status = 'DEACTIVATE';

