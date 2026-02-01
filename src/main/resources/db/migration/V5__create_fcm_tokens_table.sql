-- V5__create_fcm_tokens_table.sql
-- FCM Token storage for Push Notifications
-- Supports multi-device, role-based targeting, and token lifecycle management

-- Create sequence for FCM token IDs
CREATE SEQUENCE IF NOT EXISTS fcm_token_sequence START WITH 1 INCREMENT BY 1;

-- FCM Tokens Table
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('fcm_token_sequence'),

    -- The actual FCM registration token from the client (unique per device)
    token               VARCHAR(512) NOT NULL UNIQUE,

    -- User association (not a FK to allow flexibility and avoid cascade issues)
    user_id             BIGINT NOT NULL,

    -- User's role at registration time (for role-based notifications)
    role                VARCHAR(30) NOT NULL,

    -- Whether this token is active and should receive notifications
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    -- Last time this token was successfully used to send a notification
    last_used_at        TIMESTAMP,

    -- Optional device information for debugging
    device_info         VARCHAR(255),

    -- Audit timestamps
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_fcm_token_user_id ON fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_fcm_token_role ON fcm_tokens(role);
CREATE INDEX IF NOT EXISTS idx_fcm_token_active ON fcm_tokens(is_active);
CREATE INDEX IF NOT EXISTS idx_fcm_token_value ON fcm_tokens(token);

-- Composite index for finding active tokens by user
CREATE INDEX IF NOT EXISTS idx_fcm_token_user_active ON fcm_tokens(user_id, is_active);

-- Composite index for finding active tokens by role
CREATE INDEX IF NOT EXISTS idx_fcm_token_role_active ON fcm_tokens(role, is_active);

-- Index for cleanup of stale tokens
CREATE INDEX IF NOT EXISTS idx_fcm_token_last_used ON fcm_tokens(last_used_at);

COMMENT ON TABLE fcm_tokens IS 'Stores Firebase Cloud Messaging tokens for push notifications';
COMMENT ON COLUMN fcm_tokens.token IS 'FCM registration token from client device';
COMMENT ON COLUMN fcm_tokens.user_id IS 'User ID this token belongs to';
COMMENT ON COLUMN fcm_tokens.role IS 'User role for role-based notification targeting';
COMMENT ON COLUMN fcm_tokens.is_active IS 'Whether token should receive notifications';
COMMENT ON COLUMN fcm_tokens.last_used_at IS 'Last successful notification send timestamp';
COMMENT ON COLUMN fcm_tokens.device_info IS 'Optional browser/device identification for debugging';
