CREATE TABLE IF NOT EXISTS adoption_request (
    id UUID PRIMARY KEY,
    pet_id UUID NOT NULL,
    shelter_id UUID NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_update_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adoption_request_pet FOREIGN KEY (pet_id) REFERENCES pet(id),
    CONSTRAINT fk_adoption_request_shelter FOREIGN KEY (shelter_id) REFERENCES shelter(id)  -- Certifique-se de que a tabela 'shelter' existe
);

COMMENT ON TABLE adoption_request IS 'Tabela que armazena solicitações de adoção de pets';
COMMENT ON COLUMN adoption_request.id IS 'Identificador único da solicitação de adoção';
COMMENT ON COLUMN adoption_request.pet_id IS 'Referência ao pet da solicitação de adoção';
COMMENT ON COLUMN adoption_request.shelter_id IS 'Referência ao abrigo responsável pela adoção';
COMMENT ON COLUMN adoption_request.user_name IS 'Nome do usuário que está solicitando a adoção';
COMMENT ON COLUMN adoption_request.user_email IS 'Email do usuário que está solicitando a adoção';
COMMENT ON COLUMN adoption_request.status IS 'Status da solicitação de adoção (PENDING, APPROVED, REJECTED)';
COMMENT ON COLUMN adoption_request.request_create_date IS 'Data da criação da solicitação de adoção';
COMMENT ON COLUMN adoption_request.request_update_date IS 'Data da última atualização da solicitação';

CREATE INDEX IF NOT EXISTS idx_adoption_user_email ON adoption_request(user_email);
CREATE INDEX IF NOT EXISTS idx_adoption_pet ON adoption_request(pet_id);