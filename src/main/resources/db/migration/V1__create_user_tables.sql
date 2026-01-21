-- ===================================================================================
-- V1: Core User Tables Migration
-- Creates base user tables with inheritance structure and authentication tokens
-- ===================================================================================

-- ==================== USERS TABLE (Base for all user types) ====================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    phone_number VARCHAR(15),
    failed_login_attempts INTEGER DEFAULT 0,
    last_failed_login_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- ==================== STUDENTS TABLE ====================
CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    batch VARCHAR(50) NOT NULL,
    program VARCHAR(100) NOT NULL,
    -- FacultyType enum: COMPUTING, BUSINESS
    faculty VARCHAR(50) NOT NULL,
    student_role_type VARCHAR(30) DEFAULT 'NORMAL',
    enrollment_date DATE,
    date_of_birth DATE,
    address VARCHAR(200),
    guardian_name VARCHAR(50),
    guardian_phone VARCHAR(15),
    -- Club Member specific fields
    club_name VARCHAR(100),
    -- ClubPositionsType enum: PRESIDENT, VICE_PRESIDENT, SECRETARY, TREASURER, COMMITTEE_MEMBER, GENERAL_MEMBER
    club_position VARCHAR(50),
    club_join_date DATE,
    club_membership_id VARCHAR(50),
    -- Senior Kuppi specific fields
    kuppi_experience_level VARCHAR(20),
    kuppi_sessions_completed INTEGER,
    kuppi_rating DOUBLE PRECISION,
    kuppi_availability VARCHAR(500),
    -- Batch Rep specific fields
    batch_rep_year VARCHAR(10),
    batch_rep_semester VARCHAR(20),
    batch_rep_elected_date DATE,
    batch_rep_responsibilities VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_students_student_id ON students(student_id);
CREATE INDEX IF NOT EXISTS idx_students_batch ON students(batch);
CREATE INDEX IF NOT EXISTS idx_students_faculty ON students(faculty);
CREATE INDEX IF NOT EXISTS idx_students_role_type ON students(student_role_type);

-- Student Kuppi Subjects (ElementCollection)
CREATE TABLE IF NOT EXISTS student_kuppi_subjects (
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject VARCHAR(100) NOT NULL,
    PRIMARY KEY (student_id, subject)
);

-- ==================== LECTURERS TABLE ====================
CREATE TABLE IF NOT EXISTS lecturers (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    -- FacultyType enum: COMPUTING, BUSINESS
    faculty VARCHAR(50) NOT NULL,
    designation VARCHAR(50),
    specialization VARCHAR(50),
    join_date DATE,
    office_location VARCHAR(100),
    bio VARCHAR(500),
    available_for_meetings BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_lecturers_employee_id ON lecturers(employee_id);
CREATE INDEX IF NOT EXISTS idx_lecturers_department ON lecturers(department);
CREATE INDEX IF NOT EXISTS idx_lecturers_faculty ON lecturers(faculty);

-- Lecturer Qualifications (ElementCollection)
CREATE TABLE IF NOT EXISTS lecturer_qualifications (
    lecturer_id BIGINT NOT NULL REFERENCES lecturers(id) ON DELETE CASCADE,
    qualification VARCHAR(200) NOT NULL,
    PRIMARY KEY (lecturer_id, qualification)
);

-- ==================== ACADEMIC STAFF TABLE ====================
CREATE TABLE IF NOT EXISTS academic_staff (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    -- FacultyType enum: COMPUTING, BUSINESS
    faculty VARCHAR(50) NOT NULL,
    position VARCHAR(50) NOT NULL,
    join_date DATE,
    office_location VARCHAR(100),
    responsibilities VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_academic_staff_employee_id ON academic_staff(employee_id);

-- Academic Staff Qualifications (ElementCollection)
CREATE TABLE IF NOT EXISTS academic_staff_qualifications (
    academic_staff_id BIGINT NOT NULL REFERENCES academic_staff(id) ON DELETE CASCADE,
    qualification VARCHAR(200) NOT NULL,
    PRIMARY KEY (academic_staff_id, qualification)
);

-- ==================== NON-ACADEMIC STAFF TABLE ====================
CREATE TABLE IF NOT EXISTS non_academic_staff (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    designation VARCHAR(50),
    join_date DATE,
    office_location VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_non_academic_staff_employee_id ON non_academic_staff(employee_id);

-- ==================== ADMINS TABLE ====================
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    admin_id VARCHAR(20) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    assigned_date DATE
);

CREATE INDEX IF NOT EXISTS idx_admins_admin_id ON admins(admin_id);

-- Admin Permissions (ElementCollection)
CREATE TABLE IF NOT EXISTS admin_permissions (
    admin_id BIGINT NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (admin_id, permission)
);

-- ==================== SUPER ADMINS TABLE ====================
CREATE TABLE IF NOT EXISTS super_admins (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    admin_id VARCHAR(20) NOT NULL UNIQUE,
    department VARCHAR(100),
    assigned_date DATE
);

CREATE INDEX IF NOT EXISTS idx_super_admins_admin_id ON super_admins(admin_id);

-- ==================== COMMENTS ====================
COMMENT ON TABLE users IS 'Base table for all user types using JOINED inheritance';
COMMENT ON TABLE students IS 'Student users with academic and role-specific information';
COMMENT ON TABLE lecturers IS 'Lecturer users with academic credentials';
COMMENT ON TABLE academic_staff IS 'Academic staff members';
COMMENT ON TABLE non_academic_staff IS 'Non-academic staff members';
COMMENT ON TABLE admins IS 'Admin users with system management permissions';
COMMENT ON TABLE super_admins IS 'Super admin users with full system access';
