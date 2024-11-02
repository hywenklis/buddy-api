CREATE TABLE IF NOT EXISTS adoption_status_history (
    status_id UUID PRIMARY KEY,
    adoption_request_v2_id UUID NOT NULL,
    status_name VARCHAR(100) NOT NULL,
    member_id UUID NOT NULL,
    status_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adoption_status_history_request FOREIGN KEY (adoption_request_v2_id)
        REFERENCES adoption_request_v2(adoption_request_v2_id),
    CONSTRAINT fk_adoption_status_history_member FOREIGN KEY (member_id) REFERENCES shelter_member(member_id)
);

COMMENT ON TABLE adoption_status_history IS 'Table that tracks the status history of adoption requests';
COMMENT ON COLUMN adoption_status_history.status_id IS 'Unique identifier for each status update';
COMMENT ON COLUMN adoption_status_history.adoption_request_v2_id
    IS 'Foreign key linking to the adoption request v2 table';
COMMENT ON COLUMN adoption_status_history.status_name IS 'The name of the status (e.g., submitted, approved, etc.)';
COMMENT ON COLUMN adoption_status_history.member_id IS 'Foreign key linking to the shelter member who changed the status';
COMMENT ON COLUMN adoption_status_history.status_date IS 'The date when the status was updated';
COMMENT ON COLUMN adoption_status_history.creation_date IS 'Request creation date';
COMMENT ON COLUMN adoption_status_history.updated_date IS 'Request last update date';
