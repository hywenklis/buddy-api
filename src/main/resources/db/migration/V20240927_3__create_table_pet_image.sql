CREATE TABLE IF NOT EXISTS pet_image (
    id UUID PRIMARY KEY,
    image_url VARCHAR(255) NOT NULL,
    pet_id UUID NOT NULL,
    create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pet_image_pet FOREIGN KEY (pet_id) REFERENCES pet(id)
);

COMMENT ON TABLE pet_image IS 'Tabela que armazena imagens associadas aos pets';
COMMENT ON COLUMN pet_image.id IS 'Identificador único para cada imagem';
COMMENT ON COLUMN pet_image.image_url IS 'URL da imagem associada ao pet';
COMMENT ON COLUMN pet_image.pet_id IS 'Referência ao pet associado (chave estrangeira)';
COMMENT ON COLUMN pet_image.create_date IS 'Data de criação do registro';
COMMENT ON COLUMN pet_image.update_date IS 'Data da última atualização do registro';

CREATE INDEX IF NOT EXISTS idx_pet_image_pet ON pet_image(pet_id);