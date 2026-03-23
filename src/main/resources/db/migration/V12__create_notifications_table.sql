-- ===================================================================================
-- V12: Create notifications table and add missing fcm_tokens columns
-- Supports notification history for students to view past notifications
-- ===================================================================================

-- ==================== NOTIFICATIONS TABLE ====================

CREATE TABLE IF NOT EXISTS notifications (
    id                  BIGSERIAL PRIMARY KEY,

    -- User who received this notification
    user_id             BIGINT NOT NULL,

    -- Notification content
    title               VARCHAR(255) NOT NULL,
    body                TEXT,

    -- Notification type for categorization
    type                VARCHAR(50) NOT NULL DEFAULT 'GENERAL',

    -- Read status
    read                BOOLEAN NOT NULL DEFAULT FALSE,

    -- Optional click action URL
    click_action        VARCHAR(500),

    -- Optional image URL for rich notifications
    image_url           VARCHAR(500),

    -- Additional custom data (JSON)
    data                JSONB,

    -- Timestamps
    sent_at             TIMESTAMP WITH TIME ZONE,
    read_at             TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key to users table
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(read);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_sent_at ON notifications(sent_at);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, read);
CREATE INDEX IF NOT EXISTS idx_notifications_user_sent ON notifications(user_id, sent_at DESC);

COMMENT ON TABLE notifications IS 'Stores notification history for users to view past notifications';
COMMENT ON COLUMN notifications.type IS 'Notification type: GENERAL, KUPPI_SESSION, KUPPI_REMINDER, ANNOUNCEMENT, etc.';

-- ==================== FCM TOKENS TABLE UPDATES ====================

-- Add device_type column if not exists (used for analytics)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'fcm_tokens' AND column_name = 'device_type'
    ) THEN
        ALTER TABLE fcm_tokens ADD COLUMN device_type VARCHAR(50);
    END IF;
END $$;

COMMENT ON COLUMN fcm_tokens.device_type IS 'Device type: android, ios, web';

