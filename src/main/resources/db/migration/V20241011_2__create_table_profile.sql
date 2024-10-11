CREATE TABLE IF NOT EXISTS profile (
    profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    profile_type VARCHAR(50) NOT NULL,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_account FOREIGN KEY (account_id) REFERENCES account(account_id)
);

COMMENT ON TABLE profile IS 'Table that stores information about profiles';
COMMENT ON COLUMN profile.profile_id IS 'Unique identifier for each profile';
COMMENT ON COLUMN profile.account_id IS 'Foreign key linking to the associated account';
COMMENT ON COLUMN profile.profile_type IS 'Type of profile (SHELTER, ADOPTER, ADMIN)';
COMMENT ON COLUMN profile.creation_date IS 'Record creation date';
COMMENT ON COLUMN profile.updated_date IS 'Record last update date';

CREATE INDEX IF NOT EXISTS idx_profile_account_id ON profile(account_id);
CREATE INDEX IF NOT EXISTS idx_profile_profile_type ON profile(profile_type);
