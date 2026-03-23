-- =============================================
-- V11: Create Boarding House tables
-- =============================================

-- Main boarding house listing table
CREATE TABLE IF NOT EXISTS boarding_houses (
    id BIGINT NOT NULL DEFAULT nextval('entity_sequence'),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    price NUMERIC(10, 2) NOT NULL,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    gender_preference VARCHAR(20) NOT NULL DEFAULT 'ANY',
    total_rooms INTEGER,
    available_rooms INTEGER DEFAULT 0,
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_email VARCHAR(100),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    posted_by BIGINT NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,

    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT pk_boarding_houses PRIMARY KEY (id),
    CONSTRAINT fk_boarding_houses_posted_by FOREIGN KEY (posted_by) REFERENCES users(id)
);

-- Boarding house amenities (element collection)
CREATE TABLE IF NOT EXISTS boarding_house_amenities (
    boarding_house_id BIGINT NOT NULL,
    amenity VARCHAR(100) NOT NULL,
    CONSTRAINT fk_bh_amenities_house FOREIGN KEY (boarding_house_id) REFERENCES boarding_houses(id) ON DELETE CASCADE
);

-- Boarding house images (multiple images per listing, stored in S3)
CREATE TABLE IF NOT EXISTS boarding_house_images (
    id BIGINT NOT NULL DEFAULT nextval('entity_sequence'),
    boarding_house_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_boarding_house_images PRIMARY KEY (id),
    CONSTRAINT fk_bh_images_house FOREIGN KEY (boarding_house_id) REFERENCES boarding_houses(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_bh_city ON boarding_houses(city);
CREATE INDEX IF NOT EXISTS idx_bh_district ON boarding_houses(district);
CREATE INDEX IF NOT EXISTS idx_bh_price ON boarding_houses(price);
CREATE INDEX IF NOT EXISTS idx_bh_gender ON boarding_houses(gender_preference);
CREATE INDEX IF NOT EXISTS idx_bh_available ON boarding_houses(is_available);
CREATE INDEX IF NOT EXISTS idx_bh_deleted ON boarding_houses(is_deleted);
CREATE INDEX IF NOT EXISTS idx_bh_posted_by ON boarding_houses(posted_by);
CREATE INDEX IF NOT EXISTS idx_bh_images_house ON boarding_house_images(boarding_house_id);
CREATE INDEX IF NOT EXISTS idx_bh_amenities_house ON boarding_house_amenities(boarding_house_id);
