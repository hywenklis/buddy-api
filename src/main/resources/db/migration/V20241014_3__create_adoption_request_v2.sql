CREATE TABLE IF NOT EXISTS adoption_request_v2 (
    request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    shelter_id UUID NOT NULL,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adoption_request_v2_pet FOREIGN KEY (pet_id) REFERENCES pet_v2(pet_id),
    CONSTRAINT fk_adoption_request_v2_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    CONSTRAINT fk_adoption_request_v2_shelter FOREIGN KEY (shelter_id) REFERENCES shelter_v2(shelter_id)
);

COMMENT ON TABLE adoption_request_v2 IS 'Table that stores information about adoption requests v2';
COMMENT ON COLUMN adoption_request_v2.request_id IS 'Unique identifier for each request';
COMMENT ON COLUMN adoption_request_v2.pet_id IS 'Foreign key linking to the pet table';
COMMENT ON COLUMN adoption_request_v2.profile_id IS 'Foreign key linking to the profile table';
COMMENT ON COLUMN adoption_request_v2.shelter_id IS 'Foreign key linking to the shelter_v2 table';
COMMENT ON COLUMN adoption_request_v2.creation_date IS 'Request creation date';
COMMENT ON COLUMN adoption_request_v2.updated_date IS 'Request last update date';
