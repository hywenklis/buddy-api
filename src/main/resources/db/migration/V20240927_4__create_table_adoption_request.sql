DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'adoption_request') THEN
        CREATE TABLE adoption_request (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            pet_id UUID NOT NULL,
            shelter_id UUID NOT NULL,
            user_name VARCHAR(255) NOT NULL,
            user_email VARCHAR(255) NOT NULL,
            status VARCHAR(20) NOT NULL,
            request_create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
            request_update_date TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
        );
    END IF;
END $$;

COMMENT ON TABLE adoption_request IS 'Tabela que armazena solicitações de adoção de pets';
COMMENT ON COLUMN adoption_request.id IS 'Identificador único da solicitação de adoção';
COMMENT ON COLUMN adoption_request.pet_id IS 'Referência ao pet da solicitação de adoção';
COMMENT ON COLUMN adoption_request.shelter_id IS 'Referência ao abrigo responsável pela adoção';
COMMENT ON COLUMN adoption_request.user_name IS 'Nome do usuário que está solicitando a adoção';
COMMENT ON COLUMN adoption_request.user_email IS 'Email do usuário que está solicitando a adoção';
COMMENT ON COLUMN adoption_request.status IS 'Status da solicitação de adoção (PENDING, APPROVED, REJECTED)';
COMMENT ON COLUMN adoption_request.request_create_date IS 'Data da criação da solicitação de adoção';
COMMENT ON COLUMN adoption_request.request_update_date IS 'Data da última atualização da solicitação';

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'idx_adoption_user_email') THEN
        CREATE INDEX idx_adoption_user_email ON adoption_request(user_email);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'idx_adoption_pet') THEN
        CREATE INDEX idx_adoption_pet ON adoption_request(pet_id);
    END IF;
END $$;
