CREATE TABLE IF NOT EXISTS adoption_status_history (
    status_id UUID PRIMARY KEY,
    adoption_request_v2_id UUID NOT NULL,
    status_name VARCHAR(100) NOT NULL,
    profile_id UUID NOT NULL,
    status_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adoption_status_history_request FOREIGN KEY (adoption_request_v2_id)
        REFERENCES adoption_request_v2(adoption_request_v2_id),
    CONSTRAINT fk_adoption_status_history_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id)
);

COMMENT ON TABLE adoption_status_history IS 'Table that tracks the status history of adoption requests';
COMMENT ON COLUMN adoption_status_history.status_id IS 'Unique identifier for each status update';
COMMENT ON COLUMN adoption_status_history.adoption_request_v2_id
    IS 'Foreign key linking to the adoption request v2 table';
COMMENT ON COLUMN adoption_status_history.status_name IS 'The name of the status (e.g., submitted, approved, etc.)';
COMMENT ON COLUMN adoption_status_history.profile_id IS 'Foreign key linking to the profile who changed the status';
COMMENT ON COLUMN adoption_status_history.status_date IS 'The date when the status was updated';
COMMENT ON COLUMN adoption_status_history.creation_date IS 'Request creation date';
COMMENT ON COLUMN adoption_status_history.updated_date IS 'Request last update date';
