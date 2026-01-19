CREATE TABLE IF NOT EXISTS terms_version (
    terms_version_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    version_tag VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    publication_date TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_terms_version_tag UNIQUE (version_tag),
    CONSTRAINT fk_terms_version_account FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE UNIQUE INDEX idx_terms_version_one_active
    ON terms_version (is_active)
    WHERE is_active = true;

COMMENT ON TABLE terms_version IS 'Stores different versions of the Terms of Use and Privacy Policies of the platform.';
COMMENT ON COLUMN terms_version.terms_version_id IS 'Unique identifier for the terms version.';
COMMENT ON COLUMN terms_version.version_tag IS 'Version tag identifier (e.g., v1.0, v1.1) for semantic control.';
COMMENT ON COLUMN terms_version.content IS 'Complete text content of the terms in plain text or HTML/Markdown.';
COMMENT ON COLUMN terms_version.publication_date IS 'Date when this version became legally effective.';
COMMENT ON COLUMN terms_version.is_active IS 'Indicates if this is the currently active version for new acceptances.';
COMMENT ON COLUMN terms_version.account_id IS 'Reference to the administrator account that published/created this version.';
