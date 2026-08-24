CREATE TABLE IF NOT EXISTS security_audit_event (
    event_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    ip_address VARCHAR(255),
    user_agent TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_security_audit_event_account FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE INDEX idx_security_audit_event_account_id ON security_audit_event(account_id);
CREATE INDEX idx_security_audit_event_created_at ON security_audit_event(created_at);

COMMENT ON TABLE security_audit_event IS 'Audit table recording security-related events for accounts.';
COMMENT ON COLUMN security_audit_event.event_id IS 'Unique identifier for the audit event.';
COMMENT ON COLUMN security_audit_event.account_id IS 'Reference to the account that triggered the event.';
COMMENT ON COLUMN security_audit_event.event_type IS 'Type of the security event (e.g., PASSWORD_CHANGED, LOGIN_FAILED).';
COMMENT ON COLUMN security_audit_event.ip_address IS 'User IP address at the time of the event (must be encrypted or hashed for privacy).';
COMMENT ON COLUMN security_audit_event.user_agent IS 'Device/Browser information used at the time of the event.';
COMMENT ON COLUMN security_audit_event.created_at IS 'Exact timestamp of the event.';
