-- Voting Module Database Migration
-- Creates tables for Clubs, Club Memberships, Elections, Candidates, and Votes

-- ==================== CLUBS TABLE ====================
CREATE TABLE IF NOT EXISTS clubs (
    id BIGSERIAL PRIMARY KEY,
    club_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    logo_url VARCHAR(500),
    -- FacultyType enum: COMPUTING, BUSINESS
    faculty VARCHAR(50),
    email VARCHAR(200),
    contact_number VARCHAR(15),
    established_date DATE,
    social_media_links VARCHAR(500),
    president_id BIGINT REFERENCES students(id),
    advisor_id BIGINT REFERENCES academic_staff(id),
    max_members INTEGER NOT NULL DEFAULT 500,
    is_registration_open BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_club_code ON clubs(club_code);
CREATE INDEX IF NOT EXISTS idx_club_name ON clubs(name);
CREATE INDEX IF NOT EXISTS idx_club_faculty ON clubs(faculty);

-- ==================== CLUB MEMBERSHIPS TABLE ====================
CREATE TABLE IF NOT EXISTS club_memberships (
    id BIGSERIAL PRIMARY KEY,
    club_id BIGINT NOT NULL REFERENCES clubs(id),
    member_id BIGINT NOT NULL REFERENCES users(id),
    membership_number VARCHAR(30) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- ClubPositionsType enum: PRESIDENT, VICE_PRESIDENT, SECRETARY, TREASURER, COMMITTEE_MEMBER, GENERAL_MEMBER
    position VARCHAR(50),
    join_date DATE NOT NULL,
    expiry_date DATE,
    approved_at TIMESTAMP,
    approved_by_id BIGINT REFERENCES users(id),
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_club_member UNIQUE (club_id, member_id)
);

CREATE INDEX IF NOT EXISTS idx_membership_club ON club_memberships(club_id);
CREATE INDEX IF NOT EXISTS idx_membership_member ON club_memberships(member_id);
CREATE INDEX IF NOT EXISTS idx_membership_status ON club_memberships(status);

-- ==================== ELECTIONS TABLE ====================
CREATE TABLE IF NOT EXISTS elections (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    club_id BIGINT NOT NULL REFERENCES clubs(id),
    election_type VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    nomination_start_time TIMESTAMP NOT NULL,
    nomination_end_time TIMESTAMP NOT NULL,
    voting_start_time TIMESTAMP NOT NULL,
    voting_end_time TIMESTAMP NOT NULL,
    results_published_at TIMESTAMP,
    created_by_id BIGINT NOT NULL REFERENCES users(id),
    max_candidates INTEGER NOT NULL DEFAULT 10,
    winners_count INTEGER NOT NULL DEFAULT 1,
    is_anonymous_voting BOOLEAN NOT NULL DEFAULT true,
    require_manifesto BOOLEAN NOT NULL DEFAULT false,
    eligibility_criteria VARCHAR(1000),
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_election_club ON elections(club_id);
CREATE INDEX IF NOT EXISTS idx_election_status ON elections(status);
CREATE INDEX IF NOT EXISTS idx_election_type ON elections(election_type);
CREATE INDEX IF NOT EXISTS idx_election_voting_start ON elections(voting_start_time);
CREATE INDEX IF NOT EXISTS idx_election_voting_end ON elections(voting_end_time);

-- ==================== CANDIDATES TABLE ====================
CREATE TABLE IF NOT EXISTS candidates (
    id BIGSERIAL PRIMARY KEY,
    election_id BIGINT NOT NULL REFERENCES elections(id),
    student_id BIGINT NOT NULL REFERENCES users(id),
    manifesto VARCHAR(3000),
    slogan VARCHAR(500),
    photo_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    nominated_at TIMESTAMP,
    nominated_by_id BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,
    reviewed_by_id BIGINT REFERENCES users(id),
    rejection_reason VARCHAR(500),
    qualifications VARCHAR(1000),
    previous_experience VARCHAR(500),
    vote_count INTEGER NOT NULL DEFAULT 0,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_election_candidate UNIQUE (election_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_candidate_election ON candidates(election_id);
CREATE INDEX IF NOT EXISTS idx_candidate_student ON candidates(student_id);
CREATE INDEX IF NOT EXISTS idx_candidate_status ON candidates(status);

-- ==================== VOTES TABLE ====================
CREATE TABLE IF NOT EXISTS votes (
    id BIGSERIAL PRIMARY KEY,
    election_id BIGINT NOT NULL REFERENCES elections(id),
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    voter_id BIGINT REFERENCES users(id),
    vote_hash VARCHAR(64) NOT NULL,
    voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    voter_ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    verification_token VARCHAR(64) UNIQUE,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_election_voter UNIQUE (election_id, voter_id)
);

CREATE INDEX IF NOT EXISTS idx_vote_election ON votes(election_id);
CREATE INDEX IF NOT EXISTS idx_vote_candidate ON votes(candidate_id);
CREATE INDEX IF NOT EXISTS idx_vote_voter ON votes(voter_id);
CREATE INDEX IF NOT EXISTS idx_vote_hash ON votes(vote_hash);

-- ==================== COMMENTS ====================
COMMENT ON TABLE clubs IS 'Clubs/societies in the university';
COMMENT ON TABLE club_memberships IS 'Student memberships in clubs';
COMMENT ON TABLE elections IS 'Club elections for various positions';
COMMENT ON TABLE candidates IS 'Candidates standing in elections';
COMMENT ON TABLE votes IS 'Votes cast by members in elections';
