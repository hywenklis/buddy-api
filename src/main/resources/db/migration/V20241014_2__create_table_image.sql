CREATE TABLE IF NOT EXISTS image (
    image_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID,
    pet_id UUID,
    is_avatar BOOLEAN NOT NULL DEFAULT FALSE,
    file_path VARCHAR(255) NOT NULL,
    image_data BYTEA,
    image_status VARCHAR(50),
    display_order INTEGER NOT NULL,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_profile FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    CONSTRAINT fk_image_pet FOREIGN KEY (pet_id) REFERENCES pet_v2(pet_id),
    CONSTRAINT ck_image_single_entity CHECK (
        (profile_id is not null and pet_id is null) or
        (pet_id is not null and profile_id is null)
    ),
    CONSTRAINT uk_image_display_order UNIQUE (profile_id, pet_id, display_order)
);

COMMENT ON TABLE image IS 'Table that stores image files and related metadata';
COMMENT ON COLUMN image.image_id IS 'Unique identifier for each image';
COMMENT ON COLUMN image.profile_id IS 'Foreign key linking to profile if it is a profile avatar';
COMMENT ON COLUMN image.pet_id IS 'Foreign key linking to pet if it is a pet image';
COMMENT ON COLUMN image.is_avatar IS 'Indicates if the image is a profile avatar';
COMMENT ON COLUMN image.file_path IS 'File path of the image';
COMMENT ON COLUMN image.image_data IS 'Binary data of the image file';
COMMENT ON COLUMN image.image_status IS 'Status of the image (active, inactive, etc.)';
COMMENT ON COLUMN image.display_order IS 'Display order of the image';
COMMENT ON COLUMN image.creation_date IS 'Record creation date';
COMMENT ON COLUMN image.updated_date IS 'Record last update date';

CREATE INDEX IF NOT EXISTS idx_image_profile_id ON image(profile_id);
CREATE INDEX IF NOT EXISTS idx_image_pet_id ON image(pet_id);
CREATE INDEX IF NOT EXISTS idx_image_profile_avatar ON image(profile_id, is_avatar);
CREATE INDEX IF NOT EXISTS idx_image_pet_avatar ON image(pet_id, is_avatar);