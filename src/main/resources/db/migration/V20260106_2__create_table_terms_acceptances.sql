CREATE TABLE IF NOT EXISTS terms_acceptances (
    acceptance_id UUID PRIMARY KEY,
    terms_version_id UUID NOT NULL,
    account_id UUID NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    user_agent TEXT,
    accepted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_terms_acceptances_account FOREIGN KEY (account_id) REFERENCES account(account_id),
    CONSTRAINT fk_terms_acceptances_version FOREIGN KEY (terms_version_id) REFERENCES terms_versions(terms_version_id)
);

CREATE UNIQUE INDEX idx_terms_acceptances_user_version ON terms_acceptances(account_id, terms_version_id);

COMMENT ON TABLE terms_acceptances IS 'Audit table recording user acceptance of terms.';
COMMENT ON COLUMN terms_acceptances.ip_address IS 'User IP address at acceptance time (must be encrypted or hashed for privacy).';
COMMENT ON COLUMN terms_acceptances.user_agent IS 'Device/Browser information used at acceptance time.';
COMMENT ON COLUMN terms_acceptances.accepted_at IS 'Exact timestamp of acceptance.';
COMMENT ON COLUMN terms_acceptances.terms_version_id IS 'Reference to the specific version of terms that was accepted.';