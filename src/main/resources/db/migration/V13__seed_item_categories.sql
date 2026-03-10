-- =============================================================================
-- Lost & Found Module — Seed Data
-- Migration: V13__seed_item_categories.sql
--
-- Inserts default item categories so the Lost & Found module works immediately
-- after startup without throwing ResourceNotFoundException on every create call.
-- =============================================================================

INSERT INTO item_categories (name) VALUES
                                       ('Electronics'),
                                       ('Clothing'),
                                       ('Keys'),
                                       ('Bags'),
                                       ('Documents'),
                                       ('Accessories'),
                                       ('Books'),
                                       ('Other');