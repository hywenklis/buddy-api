-- Backfill idempotent from legacy 'pet' table to 'pet_v2' table

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') AND
       EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet_v2') THEN

        -- Insert pets having a corresponding shelter profile
        INSERT INTO pet_v2 (
            pet_v2_id,
            profile_id,
            name,
            species,
            gender,
            approximate_age,
            age_report_date,
            size,
            weight,
            is_neutered,
            is_for_adoption,
            description,
            creation_date,
            updated_date
        )
        SELECT
            p.id AS pet_v2_id,
            pr.profile_id,
            p.name,
            CASE
                WHEN UPPER(p.specie) IN ('CAT', 'GATO') THEN 'CAT'
                WHEN UPPER(p.specie) IN ('BIRD', 'PASSARO', 'PÁSSARO', 'AVE') THEN 'BIRD'
                WHEN UPPER(p.specie) IN ('REPTILE', 'REPTIL', 'RÉPTIL') THEN 'REPTILE'
                WHEN UPPER(p.specie) IN ('FISH', 'PEIXE') THEN 'FISH'
                ELSE 'DOG'
            END AS species,
            CASE
                WHEN UPPER(p.gender) IN ('MALE', 'MACHO', 'M') THEN 'MALE'
                WHEN UPPER(p.gender) IN ('FEMALE', 'FEMEA', 'FÊMEA', 'F') THEN 'FEMALE'
                ELSE 'UNDEFINED'
            END AS gender,
            CASE
                WHEN p.birth_date IS NOT NULL THEN GREATEST(0, EXTRACT(YEAR FROM age(CURRENT_DATE, p.birth_date))::INTEGER)
                ELSE NULL
            END AS approximate_age,
            CASE
                WHEN p.birth_date IS NOT NULL THEN CURRENT_DATE
                ELSE NULL
            END AS age_report_date,
            NULL AS size,
            p.weight,
            NULL AS is_neutered,
            TRUE AS is_for_adoption,
            p.description,
            COALESCE(p.create_date, CURRENT_TIMESTAMP) AS creation_date,
            COALESCE(p.update_date, CURRENT_TIMESTAMP) AS updated_date
        FROM pet p
        JOIN shelter s ON s.id = p.shelter_id
        JOIN account a ON a.email = s.email
        JOIN profile pr ON pr.account_id = a.account_id AND pr.is_deleted = FALSE
        ON CONFLICT (pet_v2_id) DO NOTHING;

        -- Migrate avatar from legacy pet into image table
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'image') THEN
            INSERT INTO image (
                image_id,
                pet_v2_id,
                is_avatar,
                file_path,
                image_status,
                display_order,
                creation_date,
                updated_date
            )
            SELECT
                gen_random_uuid() AS image_id,
                p.id AS pet_v2_id,
                TRUE AS is_avatar,
                p.avatar AS file_path,
                'APPROVED' AS image_status,
                0 AS display_order,
                COALESCE(p.create_date, CURRENT_TIMESTAMP) AS creation_date,
                COALESCE(p.update_date, CURRENT_TIMESTAMP) AS updated_date
            FROM pet p
            JOIN pet_v2 pv ON pv.pet_v2_id = p.id
            WHERE p.avatar IS NOT NULL AND p.avatar != ''
              AND NOT EXISTS (
                  SELECT 1 FROM image img
                  WHERE img.pet_v2_id = p.id AND img.file_path = p.avatar
              );
        END IF;

    END IF;
END $$;
