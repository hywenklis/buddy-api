UPDATE terms_version
SET publication_date = CURRENT_TIMESTAMP
WHERE is_active = true
  AND publication_date IS NULL;

ALTER TABLE terms_version
    ADD CONSTRAINT chk_terms_active_published
        CHECK (
            is_active = false
                OR
            (is_active = true AND publication_date IS NOT NULL)
            );
