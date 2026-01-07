CREATE TABLE IF NOT EXISTS terms_versions (
    terms_version_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    version_tag VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    publication_date TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_terms_versions_tag UNIQUE (version_tag),
    CONSTRAINT fk_terms_versions_account FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE INDEX idx_terms_versions_active ON terms_versions(is_active);

COMMENT ON TABLE terms_versions IS 'Stores different versions of the Terms of Use and Privacy Policies of the platform.';
COMMENT ON COLUMN terms_versions.terms_version_id IS 'Unique identifier for the terms version.';
COMMENT ON COLUMN terms_versions.version_tag IS 'Version tag identifier (e.g., v1.0, v1.1) for semantic control.';
COMMENT ON COLUMN terms_versions.content IS 'Complete text content of the terms in plain text or HTML/Markdown.';
COMMENT ON COLUMN terms_versions.publication_date IS 'Date when this version became legally effective.';
COMMENT ON COLUMN terms_versions.is_active IS 'Indicates if this is the currently active version for new acceptances.';
COMMENT ON COLUMN terms_versions.account_id IS 'Reference to the administrator account that published/created this version.';
