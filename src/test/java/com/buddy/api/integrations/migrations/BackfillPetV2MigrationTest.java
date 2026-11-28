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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void cleanLegacyAndV2Tables() {
        imageRepository.deleteAll();
        petV2Repository.deleteAll();
        jdbcTemplate.execute("DELETE FROM pet");
        jdbcTemplate.execute("DELETE FROM shelter");
    }

    @Test
    @DisplayName("Should execute migration resource twice idempotently without duplicating data")
    void should_backfill_legacy_pets_into_pet_v2() throws IOException {
        final var email = "legacy-shelter-" + UUID.randomUUID() + "@buddy.com";
        final var account = accountRepository.save(
            validAccountEntity().email(new EmailAddress(email)).build()
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
            shelterId, "Legacy Shelter", "Responsible", "12345678901", email
        );

        final var petId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO pet (id, name, specie, gender, location, "
                + "shelter_id, avatar, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            petId, "Legacy Rex", "Cachorro", "Macho", "SP",
            shelterId, "http://cdn.com/legacy-rex.jpg", "A good legacy dog"
        );

        final var migrationSql = loadMigrationSql();
        jdbcTemplate.execute(migrationSql);
        jdbcTemplate.execute(migrationSql);

        final var migratedPet = petV2Repository.findById(petId);
        assertThat(migratedPet).isPresent();
        assertThat(migratedPet.get().getName()).isEqualTo("Legacy Rex");
        assertThat(migratedPet.get().getSpecies()).isEqualTo(PetSpecies.DOG);
        assertThat(migratedPet.get().getGender()).isEqualTo(PetGender.MALE);
        assertThat(migratedPet.get().getGuardianProfile().getProfileId())
            .isEqualTo(profile.getProfileId());

        assertThat(petV2Repository.findAll()).hasSize(1);

        final var images = imageRepository
            .findByPetV2OrderByDisplayOrderAsc(migratedPet.get());
        assertThat(images).hasSize(1);
        assertThat(images.get(0).getFilePath()).isEqualTo("http://cdn.com/legacy-rex.jpg");
    }

    @Test
    @DisplayName("Should deterministically select oldest active SHELTER profile on multi-profile")
    void should_select_oldest_active_shelter_profile_on_multi_profile_account() throws IOException {
        final var email = "multi-profile-" + UUID.randomUUID() + "@buddy.com";
        final var account = accountRepository.save(
            validAccountEntity().email(new EmailAddress(email)).build()
        );

        final var userProfile = profileRepository.save(
            ProfileEntity.builder()
                .account(account)
                .name("Personal User Profile")
                .profileType(ProfileTypeEnum.USER)
                .isDeleted(false)
                .build()
        );

        final var oldestShelter = profileRepository.save(
            ProfileEntity.builder()
                .account(account)
                .name("Oldest Active Shelter")
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build()
        );

        final var newestShelter = profileRepository.save(
            ProfileEntity.builder()
                .account(account)
                .name("Newest Active Shelter")
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build()
        );

        updateCreationDate(userProfile.getProfileId(), LocalDateTime.of(2025, 1, 1, 10, 0));
        updateCreationDate(oldestShelter.getProfileId(), LocalDateTime.of(2025, 2, 1, 10, 0));
        updateCreationDate(newestShelter.getProfileId(), LocalDateTime.of(2025, 3, 1, 10, 0));

        final var shelterId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO shelter (id, name_shelter, name_responsible, "
                + "cpf_responsible, email) VALUES (?, ?, ?, ?, ?)",
            shelterId, "Multi Profile Shelter", "Responsible", "12345678902", email
        );

        final var petId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO pet (id, name, specie, gender, location, "
                + "shelter_id, avatar, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            petId, "Luna", "Gato", "Fêmea", "RJ",
            shelterId, null, "Quiet cat"
        );

        final var migrationSql = loadMigrationSql();
        jdbcTemplate.execute(migrationSql);
        jdbcTemplate.execute(migrationSql);

        final var migratedPet = petV2Repository.findById(petId);
        assertThat(migratedPet).isPresent();
        assertThat(migratedPet.get().getSpecies()).isEqualTo(PetSpecies.CAT);
        assertThat(migratedPet.get().getGender()).isEqualTo(PetGender.FEMALE);
        assertThat(migratedPet.get().getGuardianProfile().getProfileId())
            .isEqualTo(oldestShelter.getProfileId());
        assertThat(petV2Repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Should not migrate legacy pet to USER profile when no active SHELTER exists")
    void should_not_migrate_pet_when_no_active_shelter_profile_exists() throws IOException {
        final var email = "no-active-shelter-" + UUID.randomUUID() + "@buddy.com";
        final var account = accountRepository.save(
            validAccountEntity().email(new EmailAddress(email)).build()
        );

        profileRepository.save(
            ProfileEntity.builder()
                .account(account)
                .name("Active User Only")
                .profileType(ProfileTypeEnum.USER)
                .isDeleted(false)
                .build()
        );

        profileRepository.save(
            ProfileEntity.builder()
                .account(account)
                .name("Deleted Shelter")
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(true)
                .build()
        );

        final var shelterId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO shelter (id, name_shelter, name_responsible, "
                + "cpf_responsible, email) VALUES (?, ?, ?, ?, ?)",
            shelterId, "Deleted Shelter", "Responsible", "12345678903", email
        );

        final var petId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO pet (id, name, specie, gender, location, "
                + "shelter_id, avatar, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            petId, "Orphaned", "Cachorro", "Macho", "MG",
            shelterId, null, "Should not bind to USER"
        );

        jdbcTemplate.execute(loadMigrationSql());

        assertThat(petV2Repository.findById(petId)).isEmpty();
    }

    private String loadMigrationSql() throws IOException {
        final var migrationResource = new ClassPathResource(MIGRATION_PATH);
        return migrationResource.getContentAsString(StandardCharsets.UTF_8);
    }

    private void updateCreationDate(final UUID profileId, final LocalDateTime timestamp) {
        jdbcTemplate.update(
            "UPDATE profile SET creation_date = ? WHERE profile_id = ?",
            Timestamp.valueOf(timestamp),
            profileId
        );
    }
}
