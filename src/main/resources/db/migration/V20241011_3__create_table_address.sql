CREATE TABLE IF NOT EXISTS address (
    address_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(10),
    complement VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    federative_unit VARCHAR(50) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    neighbourhood VARCHAR(100),
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(11, 7) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_address_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    CONSTRAINT uk_profile_address UNIQUE (profile_id, latitude, longitude)
);

COMMENT ON TABLE address IS 'Table that stores address information linked to profiles';
COMMENT ON COLUMN address.address_id IS 'Unique identifier for each address';
COMMENT ON COLUMN address.profile_id IS 'Foreign key linking to the associated profile';
COMMENT ON COLUMN address.street IS 'Street name of the address';
COMMENT ON COLUMN address.number IS 'Number of the address';
COMMENT ON COLUMN address.complement IS 'Complement of the address';
COMMENT ON COLUMN address.city IS 'City of the address';
COMMENT ON COLUMN address.federative_unit IS 'State or region of the address';
COMMENT ON COLUMN address.country IS 'Country of the address';
COMMENT ON COLUMN address.postal_code IS 'Postal code of the address';
COMMENT ON COLUMN address.neighbourhood IS 'District or neighborhood of the address';
COMMENT ON COLUMN address.is_primary IS 'Indicates if the address is the primary one';
COMMENT ON COLUMN address.latitude IS 'Latitude of the address';
COMMENT ON COLUMN address.longitude IS 'Longitude of the address';
COMMENT ON COLUMN address.creation_date IS 'Record creation date';
COMMENT ON COLUMN address.updated_date IS 'Record last update date';

CREATE INDEX IF NOT EXISTS idx_address_profile_id ON address(profile_id);
CREATE INDEX IF NOT EXISTS idx_address_postal_code ON address(postal_code);
