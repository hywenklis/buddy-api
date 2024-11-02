CREATE TABLE IF NOT EXISTS adoption_request_v2 (
    adoption_request_v2_id UUID PRIMARY KEY,
    pet_v2_id UUID NOT NULL,
    adopter_profile_id UUID NOT NULL,
    pet_owner_profile_id UUID NOT NULL,
    questionnaire_id UUID NOT NULL,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adoption_request_v2_pet FOREIGN KEY (pet_v2_id) REFERENCES pet_v2(pet_v2_id),
    CONSTRAINT fk_adoption_request_v2_adopter FOREIGN KEY (adopter_profile_id)
        REFERENCES profile(profile_id),
    CONSTRAINT fk_adoption_request_v2_pet_owner FOREIGN KEY (pet_owner_profile_id)
        REFERENCES profile(profile_id),
    CONSTRAINT ck_adoption_request_v2_adopter_owner CHECK (adopter_profile_id != pet_owner_profile_id),
    CONSTRAINT uk_adoption_request_v2_questionnaire UNIQUE (questionnaire_id)
);

COMMENT ON TABLE adoption_request_v2 IS 'Table that stores information about adoption requests v2';
COMMENT ON COLUMN adoption_request_v2.adoption_request_v2_id IS 'Unique identifier for each request';
COMMENT ON COLUMN adoption_request_v2.pet_v2_id IS 'Foreign key linking to the pet table';
COMMENT ON COLUMN adoption_request_v2.adopter_profile_id IS 'Foreign key linking to the adopter profile';
COMMENT ON COLUMN adoption_request_v2.pet_owner_profile_id
    IS 'Foreign key linking to the pet owner profile';
COMMENT ON COLUMN adoption_request_v2.questionnaire_id
    IS 'Foreign key linking to the adoption questionnaire table';
COMMENT ON COLUMN adoption_request_v2.creation_date IS 'Request creation date';
COMMENT ON COLUMN adoption_request_v2.updated_date IS 'Request last update date';
