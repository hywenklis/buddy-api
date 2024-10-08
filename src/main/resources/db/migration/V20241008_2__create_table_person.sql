CREATE TABLE IF NOT EXISTS person (
    person_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    terms_of_user_consent BOOLEAN DEFAULT FALSE,
    address_id UUID,
    profile_type VARCHAR(50) NOT NULL,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_person_address FOREIGN KEY (address_id) REFERENCES address(address_id)
);

COMMENT ON TABLE person IS 'Table that stores information about people';
COMMENT ON COLUMN person.person_id IS 'Unique identifier for each person';
COMMENT ON COLUMN person.first_name IS 'Person first name';
COMMENT ON COLUMN person.last_name IS 'Person last name';
COMMENT ON COLUMN person.email IS 'Person email address';
COMMENT ON COLUMN person.phone_number IS 'Phone number';
COMMENT ON COLUMN person.password IS 'Password for account access';
COMMENT ON COLUMN person.terms_of_user_consent IS 'User consent to the terms of use and platform rules';
COMMENT ON COLUMN person.address_id IS 'Foreign key referencing the address';
COMMENT ON COLUMN person.profile_type IS 'Profile type (adopter, shelter, etc.)';
COMMENT ON COLUMN person.creation_date IS 'Record creation date';
COMMENT ON COLUMN person.updated_date IS 'Record last update date';

CREATE INDEX IF NOT EXISTS idx_person_email ON person(email);
CREATE INDEX IF NOT EXISTS idx_person_profile_type ON person(profile_type);