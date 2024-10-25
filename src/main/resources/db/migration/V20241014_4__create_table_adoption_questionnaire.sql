CREATE TABLE IF NOT EXISTS adoption_questionnaire (
    questionnaire_id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    housing_type VARCHAR(100),
    has_other_pets BOOLEAN,
    family_routine VARCHAR(255),
    previous_experience VARCHAR(255),
    motivation VARCHAR(255),
    home_visit_agreement BOOLEAN,
    follow_up_agreement BOOLEAN,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questionnaire_request FOREIGN KEY (request_id) REFERENCES adoption_request_v2(request_id)
);

COMMENT ON TABLE adoption_questionnaire IS 'Table that stores questionnaires filled out by adopters';
COMMENT ON COLUMN adoption_questionnaire.questionnaire_id IS 'Unique identifier for each questionnaire';
COMMENT ON COLUMN adoption_questionnaire.request_id IS 'Foreign key linking to adoption request';
COMMENT ON COLUMN adoption_questionnaire.housing_type IS 'Type of housing of the adopter';
COMMENT ON COLUMN adoption_questionnaire.has_other_pets IS 'Indicates if the adopter has other pets';
COMMENT ON COLUMN adoption_questionnaire.family_routine IS 'Description of the family routine';
COMMENT ON COLUMN adoption_questionnaire.previous_experience IS 'Previous experience with pets';
COMMENT ON COLUMN adoption_questionnaire.motivation IS 'Motivation for adopting a pet';
COMMENT ON COLUMN adoption_questionnaire.home_visit_agreement IS 'Agreement to allow home visits';
COMMENT ON COLUMN adoption_questionnaire.follow_up_agreement IS 'Agreement to allow follow-up checks after adoption';
COMMENT ON COLUMN adoption_questionnaire.creation_date IS 'Request creation date';
COMMENT ON COLUMN adoption_questionnaire.updated_date IS 'Request last update date';

CREATE INDEX IF NOT EXISTS idx_questionnaire_request_id ON adoption_questionnaire(request_id);
