-- Add profile picture columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture_url VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture_key VARCHAR(255);

-- Add index for faster lookups on profile picture key
CREATE INDEX IF NOT EXISTS idx_users_profile_picture_key ON users(profile_picture_key);

