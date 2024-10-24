CREATE TABLE IF NOT EXISTS shelter_v2 (
    shelter_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    shelter_name VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shelter_v2_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id)
);

ALTER TABLE shelter_v2
    ADD CONSTRAINT uq_shelter_v2_name UNIQUE (shelter_name);

CREATE INDEX IF NOT EXISTS idx_shelter_v2_profile_id ON shelter_v2(profile_id);

COMMENT ON TABLE shelter_v2 IS 'Table that stores information about shelters (v2)';
COMMENT ON COLUMN shelter_v2.shelter_id IS 'Unique identifier for each shelter';
COMMENT ON COLUMN shelter_v2.profile_id IS 'Foreign key linking to the profile table';
COMMENT ON COLUMN shelter_v2.shelter_name IS 'Shelter name';
COMMENT ON COLUMN shelter_v2.creation_date IS 'Record creation timestamp';
COMMENT ON COLUMN shelter_v2.updated_date IS 'Record last update timestamp';
