-- ===================================================================================
-- V14: Fix FCM tokens table - add device_type column and ensure ID auto-generation
-- ===================================================================================

-- Add device_type column if not exists
ALTER TABLE fcm_tokens
    ADD COLUMN IF NOT EXISTS device_type VARCHAR(50);

-- Ensure the id column has proper default from sequence
ALTER TABLE fcm_tokens
    ALTER COLUMN id SET DEFAULT nextval('fcm_token_sequence');

