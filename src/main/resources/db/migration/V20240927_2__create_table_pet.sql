CREATE TABLE IF NOT EXISTS pet (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    specie VARCHAR(255) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    birth_date DATE,
    location VARCHAR(255) NOT NULL,
    weight DECIMAL(5, 2),
    description TEXT,
    avatar VARCHAR(255),
    shelter_id UUID NOT NULL,
    create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE pet IS 'Tabela que armazena informações sobre os pets disponíveis para adoção';
COMMENT ON COLUMN pet.id IS 'Identificador único do pet';
COMMENT ON COLUMN pet.name IS 'Nome do pet';
COMMENT ON COLUMN pet.specie IS 'Espécie do pet (ex: cachorro, gato)';
COMMENT ON COLUMN pet.gender IS 'Gênero do pet (macho ou fêmea)';
COMMENT ON COLUMN pet.birth_date IS 'Data de nascimento do pet';
COMMENT ON COLUMN pet.location IS 'Localização atual do pet (cidade, sigla do estado)';
COMMENT ON COLUMN pet.weight IS 'Peso do pet em quilogramas (kg)';
COMMENT ON COLUMN pet.description IS 'Descrição detalhada do pet';
COMMENT ON COLUMN pet.avatar IS 'URL da imagem do pet';
COMMENT ON COLUMN pet.shelter_id IS 'Referência para o abrigo onde o pet está localizado';
COMMENT ON COLUMN pet.create_date IS 'Data de criação do registro';
COMMENT ON COLUMN pet.update_date IS 'Data da última atualização do registro';

CREATE INDEX IF NOT EXISTS idx_pet_name ON pet(name);
CREATE INDEX IF NOT EXISTS idx_pet_shelter ON pet(shelter_id);