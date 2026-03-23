-- =============================================================================
-- Lost & Found Module — Initial Schema
-- Migration: V1__create_lost_and_found_tables.sql
--
-- Creates all tables needed by the Lost & Found module.
-- Once this migration runs, IntelliJ will resolve all @Table and @Column
-- references in the entity classes (Claim, LostItem, FoundItem, etc.)
-- and the "Cannot resolve table / column" inspection warnings will disappear.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- item_categories
-- Lookup table for item categories (Electronics, Clothing, Keys, etc.)
-- Referenced by both lost_items and found_items.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_categories (
                                               id   BIGINT       NOT NULL AUTO_INCREMENT,
                                               name VARCHAR(100) NOT NULL,

    CONSTRAINT pk_item_categories PRIMARY KEY (id),
    -- Category names must be unique so services can look them up by name safely
    CONSTRAINT uq_item_categories_name UNIQUE (name)
    );

-- -----------------------------------------------------------------------------
-- lost_items
-- Rows inserted when a student reports a lost item on campus.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lost_items (
                                          id             BIGINT       NOT NULL AUTO_INCREMENT,
                                          title          VARCHAR(200) NOT NULL,
    description    VARCHAR(1000),
    location       VARCHAR(300),
    contact_number VARCHAR(100) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    category_id    BIGINT,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,

    CONSTRAINT pk_lost_items        PRIMARY KEY (id),
    CONSTRAINT fk_lost_items_category
    FOREIGN KEY (category_id) REFERENCES item_categories (id)
    ON DELETE SET NULL
    );

-- -----------------------------------------------------------------------------
-- found_items
-- Rows inserted when a student reports a found item on campus.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS found_items (
                                           id             BIGINT       NOT NULL AUTO_INCREMENT,
                                           title          VARCHAR(200) NOT NULL,
    description    VARCHAR(1000),
    location       VARCHAR(300),
    contact_number VARCHAR(100) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    category_id    BIGINT,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,

    CONSTRAINT pk_found_items       PRIMARY KEY (id),
    CONSTRAINT fk_found_items_category
    FOREIGN KEY (category_id) REFERENCES item_categories (id)
    ON DELETE SET NULL
    );

-- -----------------------------------------------------------------------------
-- claims
-- Rows inserted when a student believes a found item is theirs.
-- Lifecycle: PENDING → APPROVED or REJECTED by admin.
--
-- Column names must match the @Column(name = "...") values on the Claim entity:
--   claimantId     → claimant_id
--   claimantName   → claimant_name
--   proofDescription → proof_description
--   rejectionReason  → rejection_reason
--   createdAt      → created_at
--   updatedAt      → updated_at
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS claims (
                                      id               BIGINT       NOT NULL AUTO_INCREMENT,
                                      lost_item_id     BIGINT       NOT NULL,
                                      found_item_id    BIGINT       NOT NULL,
                                      claimant_id      BIGINT       NOT NULL,
                                      claimant_name    VARCHAR(200),
    proof_description VARCHAR(1000),
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,

    CONSTRAINT pk_claims            PRIMARY KEY (id),
    CONSTRAINT fk_claims_lost_item
    FOREIGN KEY (lost_item_id)  REFERENCES lost_items  (id),
    CONSTRAINT fk_claims_found_item
    FOREIGN KEY (found_item_id) REFERENCES found_items (id)
    );

-- -----------------------------------------------------------------------------
-- item_images
-- Photos attached to a lost or found item.
-- Exactly one of lost_item_id or found_item_id is non-null per row.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_images (
                                           id            BIGINT       NOT NULL AUTO_INCREMENT,
                                           image_url     VARCHAR(500) NOT NULL,
    display_order INT          NOT NULL DEFAULT 0,
    lost_item_id  BIGINT,
    found_item_id BIGINT,

    CONSTRAINT pk_item_images       PRIMARY KEY (id),
    CONSTRAINT fk_item_images_lost
    FOREIGN KEY (lost_item_id)  REFERENCES lost_items  (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_images_found
    FOREIGN KEY (found_item_id) REFERENCES found_items (id) ON DELETE CASCADE
    );

-- -----------------------------------------------------------------------------
-- item_matches
-- Records a computed similarity score between a lost item and a found item.
-- Created by ItemMatchingService when it detects likely matches.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_matches (
                                            id            BIGINT        NOT NULL AUTO_INCREMENT,
                                            lost_item_id  BIGINT        NOT NULL,
                                            found_item_id BIGINT        NOT NULL,
                                            score         DOUBLE        NOT NULL,
                                            match_reason  VARCHAR(500),
    created_at    DATETIME      NOT NULL,

    CONSTRAINT pk_item_matches      PRIMARY KEY (id),
    CONSTRAINT fk_item_matches_lost
    FOREIGN KEY (lost_item_id)  REFERENCES lost_items  (id),
    CONSTRAINT fk_item_matches_found
    FOREIGN KEY (found_item_id) REFERENCES found_items (id),
    -- Prevent duplicate match records for the same lost/found pair
    CONSTRAINT uq_item_matches_pair UNIQUE (lost_item_id, found_item_id)
    );