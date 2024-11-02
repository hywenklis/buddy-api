CREATE TABLE IF NOT EXISTS pet_v2 (
    pet_v2_id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    approximate_age INTEGER,
    age_report_date DATE,
    size DECIMAL(10, 2),
    weight DECIMAL(10, 2),
    is_neutered BOOLEAN,
    is_for_adoption BOOLEAN NOT NULL,
    description TEXT,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pet_v2_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    CONSTRAINT ck_pet_v2_gender CHECK (gender IN ('MALE', 'FEMALE', 'UNKNOWN'))
);

COMMENT ON TABLE pet_v2 IS 'Table that stores information about pets (version 2)';
COMMENT ON COLUMN pet_v2.pet_v2_id IS 'Unique identifier for each pet';
COMMENT ON COLUMN pet_v2.profile_id IS 'Foreign key linking to the pet owner profile';
COMMENT ON COLUMN pet_v2.name IS 'Pets name';
COMMENT ON COLUMN pet_v2.species IS 'Pets species (e.g., dog, cat, etc.)';
COMMENT ON COLUMN pet_v2.gender IS 'MALE | FEMALE | UNKNOWN';
COMMENT ON COLUMN pet_v2.approximate_age IS 'Pets approximate age';
COMMENT ON COLUMN pet_v2.age_report_date IS 'Date when the age was estimated';
COMMENT ON COLUMN pet_v2.size IS 'Pets size in centimeters';
COMMENT ON COLUMN pet_v2.weight IS 'Pets weight in kilograms';
COMMENT ON COLUMN pet_v2.is_neutered IS 'Indicates if the pet is neutered';
COMMENT ON COLUMN pet_v2.is_for_adoption IS 'Indicates if the pet is available for adoption';
COMMENT ON COLUMN pet_v2.description IS 'Description of the pet';
COMMENT ON COLUMN pet_v2.creation_date IS 'The date when the pet record was created';
COMMENT ON COLUMN pet_v2.updated_date IS 'The date when the pet record was last updated';

CREATE INDEX IF NOT EXISTS idx_pet_v2_profile_id ON pet_v2(profile_id);
CREATE INDEX IF NOT EXISTS idx_pet_v2_name ON pet_v2(name);
