CREATE TABLE IF NOT EXISTS address (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    federative_unit VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    district VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE address IS 'Table that stores address information';
COMMENT ON COLUMN address.address_id IS 'Unique identifier for each address';
COMMENT ON COLUMN address.street IS 'Street name';
COMMENT ON COLUMN address.city IS 'City name';
COMMENT ON COLUMN address.federative_unit IS 'Federative unit or state';
COMMENT ON COLUMN address.postal_code IS 'Postal code (ZIP code)';
COMMENT ON COLUMN address.district IS 'District or neighborhood';
COMMENT ON COLUMN address.is_primary IS 'Whether this is the primary address or not';
COMMENT ON COLUMN address.creation_date IS 'Record creation date';
COMMENT ON COLUMN address.updated_date IS 'Record last update date';
