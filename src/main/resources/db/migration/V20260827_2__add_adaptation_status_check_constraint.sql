ALTER TABLE adoption_post_follow_up
    ALTER COLUMN adaptation_status TYPE VARCHAR(50);

ALTER TABLE adoption_post_follow_up
    ADD CONSTRAINT ck_adaptation_status
    CHECK (adaptation_status IN ('IN_PROGRESS', 'ADAPTED', 'STRUGGLING', 'RETURNED'));
