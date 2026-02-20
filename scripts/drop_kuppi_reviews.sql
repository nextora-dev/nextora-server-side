-- Drop the old kuppi_reviews table and let Hibernate recreate it with the new schema
-- Run this script in your PostgreSQL database

-- First drop the table
DROP TABLE IF EXISTS kuppi_reviews CASCADE;

-- After running this script:
-- 1. Restart your Spring Boot application
-- 2. Hibernate will automatically recreate the table with the new simplified schema
-- 3. The new table will only have: id, session_id, reviewer_id, tutor_id, rating, comment, tutor_response, tutor_response_at, created_at, updated_at, is_deleted, is_active, deleted_at, deleted_by

