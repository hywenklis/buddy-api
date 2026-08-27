-- Rename status_name to adoption_status to match JPA entity mapping
ALTER TABLE adoption_status_history
    RENAME COLUMN status_name TO adoption_status;

-- Resize column to match project standard
ALTER TABLE adoption_status_history
    ALTER COLUMN adoption_status TYPE VARCHAR(50);

-- Add check constraint aligned with AdoptionStatus enum values
ALTER TABLE adoption_status_history
    ADD CONSTRAINT ck_adoption_status_history_status
    CHECK (adoption_status IN ('PENDING', 'APPROVED', 'REJECTED'));
