CREATE TABLE IF NOT EXISTS adoption_post_follow_up (
    follow_up_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID NOT NULL,
    visit_date DATE,
    report VARCHAR(255),
    adaptation_status VARCHAR(100),
    recommendations TEXT,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_up_request FOREIGN KEY (request_id) REFERENCES adoption_request_v2(request_id)
);

COMMENT ON TABLE adoption_post_follow_up IS 'Table that stores post-adoption follow-up records';
COMMENT ON COLUMN adoption_post_follow_up.follow_up_id IS 'Unique identifier for each follow-up';
COMMENT ON COLUMN adoption_post_follow_up.request_id IS 'Foreign key linking to adoption request';
COMMENT ON COLUMN adoption_post_follow_up.visit_date IS 'Date of the follow-up visit';
COMMENT ON COLUMN adoption_post_follow_up.report IS 'Follow-up visit report';
COMMENT ON COLUMN adoption_post_follow_up.adaptation_status IS 'Status of the pet adaptation';
COMMENT ON COLUMN adoption_post_follow_up.recommendations IS 'Recommendations provided after follow-up';
COMMENT ON COLUMN adoption_post_follow_up.creation_date IS 'Request creation date';
COMMENT ON COLUMN adoption_post_follow_up.updated_date IS 'Request last update date';

CREATE INDEX IF NOT EXISTS idx_follow_up_request_id ON adoption_post_follow_up(request_id);
