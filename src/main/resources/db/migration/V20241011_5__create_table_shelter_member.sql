CREATE TABLE IF NOT EXISTS shelter_member (
    member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    shelter_profile_id UUID NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    enty_date TIMESTAMP WITHOUT TIME ZONE,
    departure_date TIMESTAMP WITHOUT TIME ZONE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shelter_member_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    CONSTRAINT fk_shelter_member_shelter FOREIGN KEY (shelter_profile_id) REFERENCES profile(profile_id),
    CONSTRAINT ck_shelter_member_profile_shelter CHECK (profile_id != shelter_profile_id),
    CONSTRAINT uk_shelter_member_profile UNIQUE (shelter_profile_id, profile_id)
);

COMMENT ON TABLE shelter_member IS 'Table that stores information about members of shelters';
COMMENT ON COLUMN shelter_member.member_id IS 'Unique identifier for each shelter member';
COMMENT ON COLUMN shelter_member.profile_id IS 'Foreign key linking to the member profile';
COMMENT ON COLUMN shelter_member.shelter_profile_id IS 'Foreign key linking to the shelter profile';
COMMENT ON COLUMN shelter_member.is_admin IS 'Indicates if the member is an admin of the shelter profile';
COMMENT ON COLUMN shelter_member.enty_date IS 'Date when the profile accepted being a member';
COMMENT ON COLUMN shelter_member.departure_date IS 'Date when the profile stopped being a member';
COMMENT ON COLUMN shelter_member.creation_date IS 'Record creation timestamp';
COMMENT ON COLUMN shelter_member.updated_date IS 'Last update timestamp';

CREATE INDEX IF NOT EXISTS idx_shelter_member_shelter ON shelter_member(shelter_profile_id);