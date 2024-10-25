CREATE TABLE IF NOT EXISTS shelter (
    id UUID PRIMARY KEY,
    name_shelter VARCHAR(255) NOT NULL,
    name_responsible VARCHAR(255) NOT NULL,
    cpf_responsible VARCHAR(14) NOT NULL UNIQUE,
    address VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255) NOT NULL UNIQUE,
    avatar VARCHAR(255),
    create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE shelter IS 'Tabela que armazena informações sobre abrigos de animais';
COMMENT ON COLUMN shelter.id IS 'Identificador único do abrigo';
COMMENT ON COLUMN shelter.name_shelter IS 'Nome do abrigo de animais';
COMMENT ON COLUMN shelter.name_responsible IS 'Nome do responsável pelo abrigo';
COMMENT ON COLUMN shelter.cpf_responsible IS 'CPF do responsável, valor único';
COMMENT ON COLUMN shelter.address IS 'Endereço completo do abrigo';
COMMENT ON COLUMN shelter.phone_number IS 'Número de telefone para contato com o abrigo';
COMMENT ON COLUMN shelter.email IS 'Email para contato com o abrigo, valor único';
COMMENT ON COLUMN shelter.avatar IS 'URL da imagem de avatar do abrigo';
COMMENT ON COLUMN shelter.create_date IS 'Data de criação do registro';
COMMENT ON COLUMN shelter.update_date IS 'Data da última atualização do registro';

CREATE INDEX IF NOT EXISTS idx_shelter_email ON shelter(email);
CREATE INDEX IF NOT EXISTS idx_shelter_cpf ON shelter(cpf_responsible);
