-- ===================================================================================
-- V3: Kuppi Module Tables Migration
-- Creates tables for Kuppi sessions (tutoring) and notes
-- ===================================================================================

-- ==================== KUPPI SESSIONS TABLE ====================
CREATE TABLE IF NOT EXISTS kuppi_sessions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    subject VARCHAR(100) NOT NULL,
    session_type VARCHAR(20) NOT NULL DEFAULT 'LIVE',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    host_id BIGINT NOT NULL REFERENCES students(id),
    scheduled_start_time TIMESTAMP NOT NULL,
    scheduled_end_time TIMESTAMP NOT NULL,
    live_link VARCHAR(500) NOT NULL,
    meeting_platform VARCHAR(200),
    view_count BIGINT NOT NULL DEFAULT 0,
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kuppi_session_host ON kuppi_sessions(host_id);
CREATE INDEX IF NOT EXISTS idx_kuppi_session_status ON kuppi_sessions(status);
CREATE INDEX IF NOT EXISTS idx_kuppi_session_subject ON kuppi_sessions(subject);
CREATE INDEX IF NOT EXISTS idx_kuppi_session_type ON kuppi_sessions(session_type);
CREATE INDEX IF NOT EXISTS idx_kuppi_session_start ON kuppi_sessions(scheduled_start_time);

-- ==================== KUPPI NOTES TABLE ====================
CREATE TABLE IF NOT EXISTS kuppi_notes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    file_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500),
    file_name VARCHAR(100),
    file_size BIGINT,
    session_id BIGINT REFERENCES kuppi_sessions(id) ON DELETE SET NULL,
    uploaded_by BIGINT NOT NULL REFERENCES students(id),
    allow_download BOOLEAN NOT NULL DEFAULT true,
    download_count BIGINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kuppi_note_session ON kuppi_notes(session_id);
CREATE INDEX IF NOT EXISTS idx_kuppi_note_uploader ON kuppi_notes(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_kuppi_note_file_type ON kuppi_notes(file_type);

-- ==================== KUPPI SESSION PARTICIPANTS TABLE ====================
CREATE TABLE IF NOT EXISTS kuppi_session_participants (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES kuppi_sessions(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    attendance_status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    CONSTRAINT uk_session_participant UNIQUE (session_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_kuppi_participant_session ON kuppi_session_participants(session_id);
CREATE INDEX IF NOT EXISTS idx_kuppi_participant_student ON kuppi_session_participants(student_id);

-- ==================== KUPPI SESSION FEEDBACK TABLE ====================
CREATE TABLE IF NOT EXISTS kuppi_session_feedback (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES kuppi_sessions(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_session_feedback UNIQUE (session_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_kuppi_feedback_session ON kuppi_session_feedback(session_id);
CREATE INDEX IF NOT EXISTS idx_kuppi_feedback_student ON kuppi_session_feedback(student_id);

-- ==================== KUPPI SESSION TAGS TABLE ====================
CREATE TABLE IF NOT EXISTS kuppi_session_tags (
    session_id BIGINT NOT NULL REFERENCES kuppi_sessions(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (session_id, tag)
);

CREATE INDEX IF NOT EXISTS idx_kuppi_tags_tag ON kuppi_session_tags(tag);

-- ==================== COMMENTS ====================
COMMENT ON TABLE kuppi_sessions IS 'Kuppi tutoring sessions hosted by senior students';
COMMENT ON TABLE kuppi_notes IS 'Notes and materials shared during Kuppi sessions';
COMMENT ON TABLE kuppi_session_participants IS 'Students who registered/attended Kuppi sessions';
COMMENT ON TABLE kuppi_session_feedback IS 'Feedback and ratings for Kuppi sessions';
COMMENT ON TABLE kuppi_session_tags IS 'Tags associated with Kuppi sessions for categorization';
