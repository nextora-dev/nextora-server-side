-- ===================================================================================
-- V13: Add Academic Results columns to Kuppi Applications
-- Stores S3 reference for uploaded academic result documents
-- ===================================================================================

ALTER TABLE kuppi_applications
    ADD COLUMN IF NOT EXISTS academic_results_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS academic_results_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS academic_results_file_name VARCHAR(255);

