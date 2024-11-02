CREATE TABLE IF NOT EXISTS account (
    account_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    terms_of_user_consent BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMP WITHOUT TIME ZONE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE account IS 'Table that stores information about accounts';
COMMENT ON COLUMN account.account_id IS 'Unique identifier for each account';
COMMENT ON COLUMN account.email IS 'Account holder email address';
COMMENT ON COLUMN account.phone_number IS 'Account holder phone number';
COMMENT ON COLUMN account.password IS 'Password for account access';
COMMENT ON COLUMN account.terms_of_user_consent IS 'User consent to the terms of use and platform rules';
COMMENT ON COLUMN account.is_verified IS 'Indicates if the account is verified';
COMMENT ON COLUMN account.is_blocked IS 'Indicates if the account is blocked';
COMMENT ON COLUMN account.is_deleted IS 'Indicates if the account is deleted';
COMMENT ON COLUMN account.last_login IS 'Last date when the account was authenticated';
COMMENT ON COLUMN account.creation_date IS 'Record creation date';
COMMENT ON COLUMN account.updated_date IS 'Record last update date';

CREATE INDEX IF NOT EXISTS idx_account_email ON account(email);