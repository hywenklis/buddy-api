CREATE TABLE IF NOT EXISTS account (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    terms_of_user_consent BOOLEAN DEFAULT FALSE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE account IS 'Table that stores information about accounts';
COMMENT ON COLUMN account.account_id IS 'Unique identifier for each account';
COMMENT ON COLUMN account.first_name IS 'Account holder first name';
COMMENT ON COLUMN account.last_name IS 'Account holder last name';
COMMENT ON COLUMN account.email IS 'Account holder email address';
COMMENT ON COLUMN account.phone_number IS 'Account holder phone number';
COMMENT ON COLUMN account.password IS 'Password for account access';
COMMENT ON COLUMN account.terms_of_user_consent IS 'User consent to the terms of use and platform rules';
COMMENT ON COLUMN account.creation_date IS 'Record creation date';
COMMENT ON COLUMN account.updated_date IS 'Record last update date';

CREATE INDEX IF NOT EXISTS idx_account_email ON account(email);