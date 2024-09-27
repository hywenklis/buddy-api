DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet_image') THEN
        CREATE TABLE pet_image (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            image_url VARCHAR(255) NOT NULL,
            pet_id UUID NOT NULL,
            create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
            update_date TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
        );
    END IF;
END $$;

COMMENT ON TABLE pet_image IS 'Tabela que armazena imagens associadas aos pets';
COMMENT ON COLUMN pet_image.id IS 'Identificador único para cada imagem';
COMMENT ON COLUMN pet_image.image_url IS 'URL da imagem associada ao pet';
COMMENT ON COLUMN pet_image.pet_id IS 'Referência ao pet associado (chave estrangeira)';
COMMENT ON COLUMN pet_image.create_date IS 'Data de criação do registro';
COMMENT ON COLUMN pet_image.update_date IS 'Data da última atualização do registro';

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.constraint_column_usage WHERE table_name = 'pet_image' AND column_name = 'pet_id') THEN
        ALTER TABLE pet_image ADD CONSTRAINT fk_pet_image_pet FOREIGN KEY (pet_id) REFERENCES pet(id);
    END IF;
END $$;
