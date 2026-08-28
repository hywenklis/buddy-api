package com.buddy.api.integrations.domains.pet.repositories;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.specifications.v2.PetV2BasicSpecifications;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@DisplayName("PetV2Repository — Integration Tests")
class PetV2RepositoryTest extends IntegrationTestAbstract {

    @Autowired
    private PetV2Repository petV2Repository;

    private ProfileEntity shelterProfile;

    @BeforeEach
    void setUp() {
        petV2Repository.deleteAll();
        profileRepository.deleteAll();
        accountRepository.deleteAll();

        final var account = accountRepository.save(validAccountEntity().build());

        shelterProfile = profileRepository.save(
            ProfileEntity.builder()
                .account(account)
                .name("Rescue Sanctuary")
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build()
        );
    }

    @Test
    @DisplayName("Should persist and retrieve PetV2Entity successfully")
    void should_persist_and_retrieve_pet_v2() {
        // Arrange
        final var pet = PetV2Entity.builder()
            .guardianProfile(shelterProfile)
            .name("Zeus")
            .species(PetSpecies.DOG)
            .gender(PetGender.MALE)
            .approximateAge(4)
            .ageReportDate(LocalDate.of(2026, 1, 15))
            .size(BigDecimal.valueOf(60.0))
            .weight(BigDecimal.valueOf(25.0))
            .isNeutered(true)
            .isForAdoption(true)
            .description("Gentle giant")
            .build();

        // Act
        final var saved = petV2Repository.save(pet);
        final var found = petV2Repository.findById(saved.getPetV2Id());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Zeus");
        assertThat(found.get().getSpecies()).isEqualTo(PetSpecies.DOG);
        assertThat(found.get().getGender()).isEqualTo(PetGender.MALE);
        assertThat(found.get().getApproximateAge()).isEqualTo(4);
        assertThat(found.get().getIsNeutered()).isTrue();
        assertThat(found.get().getIsForAdoption()).isTrue();
        assertThat(found.get().getGuardianProfile().getProfileId())
            .isEqualTo(shelterProfile.getProfileId());
    }

    @Test
    @DisplayName("Should filter PetV2Entity with PetV2Specifications")
    void should_filter_pet_v2_with_specifications() {
        // Arrange
        final var dog = petV2Repository.save(PetV2Entity.builder()
            .guardianProfile(shelterProfile)
            .name("Thor")
            .species(PetSpecies.DOG)
            .gender(PetGender.MALE)
            .isForAdoption(true)
            .isNeutered(true)
            .build());

        petV2Repository.save(PetV2Entity.builder()
            .guardianProfile(shelterProfile)
            .name("Mimi")
            .species(PetSpecies.CAT)
            .gender(PetGender.FEMALE)
            .isForAdoption(true)
            .isNeutered(false)
            .build());

        // Act
        final var spec = PetV2BasicSpecifications.hasSpecies(PetSpecies.DOG)
            .and(PetV2BasicSpecifications.isNeutered(true));
        final var page = petV2Repository.findAll(spec, PageRequest.of(0, 10));

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1L);
        assertThat(page.getContent().get(0).getPetV2Id()).isEqualTo(dog.getPetV2Id());
        assertThat(page.getContent().get(0).getName()).isEqualTo("Thor");
    }
}
