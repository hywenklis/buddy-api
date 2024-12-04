CREATE TABLE IF NOT EXISTS account_block_reason (
    account_block_reason_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    reason TEXT NOT NULL,
    block_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reactivation_date TIMESTAMP WITHOUT TIME ZONE,
    deletion_date TIMESTAMP WITHOUT TIME ZONE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_block_reason_account FOREIGN KEY (account_id)
        REFERENCES account(account_id),
    CONSTRAINT ck_account_block_reason_result
        CHECK (reactivation_date is null or deletion_date is null)
);

COMMENT ON TABLE account_block_reason IS 'Table that stores the reasons why an account was blocked';
COMMENT ON COLUMN account_block_reason.account_block_reason_id IS 'Unique identifier for each block reason';
COMMENT ON COLUMN account_block_reason.account_id IS 'Foreign key linking to the blocked account';
COMMENT ON COLUMN account_block_reason.reason IS 'Reason for the block';
COMMENT ON COLUMN account_block_reason.block_date IS 'Date when the account was blocked';
COMMENT ON COLUMN account_block_reason.reactivation_date
    IS 'Date when the account was reactivated (if the block was removed)';
COMMENT ON COLUMN account_block_reason.deletion_date
    IS 'Date when the account was deleted (if it was deleted because of the block)';
COMMENT ON COLUMN account_block_reason.creation_date IS 'Record creation date';
COMMENT ON COLUMN account_block_reason.updated_date IS 'Record last update date';