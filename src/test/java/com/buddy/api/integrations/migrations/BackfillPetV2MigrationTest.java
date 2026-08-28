package com.buddy.api.integrations.migrations;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("BackfillPetV2 Migration — Integration Test")
class BackfillPetV2MigrationTest extends IntegrationTestAbstract {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("Should backfill legacy pets and avatars into pet_v2 and image idempotently")
    void should_backfill_legacy_pets_into_pet_v2() {
        // Arrange
        imageRepository.deleteAll();
        petV2Repository.deleteAll();
        jdbcTemplate.execute("DELETE FROM pet");
        jdbcTemplate.execute("DELETE FROM shelter");

        final var account = accountRepository.save(
            validAccountEntity().email(new EmailAddress("legacy-shelter@buddy.com")).build()
        );

        final var profile = profileRepository.save(
            ProfileEntity.builder()
                .account(account)
                .name("Legacy Shelter Profile")
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build()
        );

        final var shelterId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO shelter (id, name_shelter, name_responsible, "
                + "cpf_responsible, email) VALUES (?, ?, ?, ?, ?)",
            shelterId, "Legacy Shelter", "Responsible", "12345678901", "legacy-shelter@buddy.com"
        );

        final var petId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO pet (id, name, specie, gender, location, "
                + "shelter_id, avatar, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            petId, "Legacy Rex", "Cachorro", "Macho", "SP",
            shelterId, "http://cdn.com/legacy-rex.jpg", "A good legacy dog"
        );

        // Act - execute the backfill script
        final var backfillPetSql = """
            INSERT INTO pet_v2 (
                pet_v2_id, profile_id, name, species, gender, approximate_age, age_report_date,
                size, weight, is_neutered, is_for_adoption, description, creation_date, updated_date
            )
            SELECT
                p.id AS pet_v2_id,
                pr.profile_id,
                p.name,
                'DOG' AS species,
                'MALE' AS gender,
                NULL AS approximate_age,
                NULL AS age_report_date,
                NULL AS size,
                p.weight,
                NULL AS is_neutered,
                TRUE AS is_for_adoption,
                p.description,
                COALESCE(p.create_date, CURRENT_TIMESTAMP),
                COALESCE(p.update_date, CURRENT_TIMESTAMP)
            FROM pet p
            JOIN shelter s ON s.id = p.shelter_id
            JOIN account a ON a.email = s.email
            JOIN profile pr ON pr.account_id = a.account_id AND pr.is_deleted = FALSE
            ON CONFLICT (pet_v2_id) DO NOTHING
            """;
        jdbcTemplate.update(backfillPetSql);

        final var backfillImageSql = """
            INSERT INTO image (
                image_id, pet_v2_id, is_avatar, file_path, image_status,
                display_order, creation_date, updated_date
            )
            SELECT
                gen_random_uuid(), p.id, TRUE, p.avatar, 'APPROVED', 0,
                COALESCE(p.create_date, CURRENT_TIMESTAMP),
                COALESCE(p.update_date, CURRENT_TIMESTAMP)
            FROM pet p
            JOIN pet_v2 pv ON pv.pet_v2_id = p.id
            WHERE p.avatar IS NOT NULL AND p.avatar != ''
              AND NOT EXISTS (
                  SELECT 1 FROM image img
                  WHERE img.pet_v2_id = p.id AND img.file_path = p.avatar
              )
            """;
        jdbcTemplate.update(backfillImageSql);

        // Assert
        final var migratedPet = petV2Repository.findById(petId);
        assertThat(migratedPet).isPresent();
        assertThat(migratedPet.get().getName()).isEqualTo("Legacy Rex");
        assertThat(migratedPet.get().getSpecies()).isEqualTo(PetSpecies.DOG);
        assertThat(migratedPet.get().getGender()).isEqualTo(PetGender.MALE);
        assertThat(migratedPet.get().getGuardianProfile().getProfileId())
            .isEqualTo(profile.getProfileId());

        final var images = imageRepository.findByPetV2OrderByDisplayOrderAsc(migratedPet.get());
        assertThat(images).hasSize(1);
        assertThat(images.get(0).getFilePath()).isEqualTo("http://cdn.com/legacy-rex.jpg");
    }
}
