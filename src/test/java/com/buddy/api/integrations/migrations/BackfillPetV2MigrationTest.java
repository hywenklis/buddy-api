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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("BackfillPetV2 Migration — Integration Test")
class BackfillPetV2MigrationTest extends IntegrationTestAbstract {

    private static final String MIGRATION_PATH =
        "db/migration/V20260827_4__backfill_pet_to_pet_v2.sql";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("Should execute migration resource twice idempotently without duplicating data")
    void should_backfill_legacy_pets_into_pet_v2() throws IOException {
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

        // Act - execute the real migration script twice to verify DO $$ blocks and idempotency
        final var migrationResource = new ClassPathResource(MIGRATION_PATH);
        final var migrationSql = migrationResource.getContentAsString(StandardCharsets.UTF_8);

        jdbcTemplate.execute(migrationSql);
        jdbcTemplate.execute(migrationSql);

        // Assert
        final var migratedPet = petV2Repository.findById(petId);
        assertThat(migratedPet).isPresent();
        assertThat(migratedPet.get().getName()).isEqualTo("Legacy Rex");
        assertThat(migratedPet.get().getSpecies()).isEqualTo(PetSpecies.DOG);
        assertThat(migratedPet.get().getGender()).isEqualTo(PetGender.MALE);
        assertThat(migratedPet.get().getGuardianProfile().getProfileId())
            .isEqualTo(profile.getProfileId());

        final var allPets = petV2Repository.findAll();
        assertThat(allPets).hasSize(1);

        final var images = imageRepository
            .findByPetV2OrderByDisplayOrderAsc(migratedPet.get());
        assertThat(images).hasSize(1);
        assertThat(images.get(0).getFilePath()).isEqualTo("http://cdn.com/legacy-rex.jpg");
    }
}
