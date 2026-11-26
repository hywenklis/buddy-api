package com.buddy.api.integrations.web.pets.v2.controllers;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.commons.exceptions.ActiveShelterProfileNotFoundException;
import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.pet.dtos.v2.CreatePetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.UpdatePetV2Dto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.services.v2.CreatePetV2;
import com.buddy.api.domains.pet.services.v2.UpdatePetV2;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.pets.v2.requests.CreatePetV2Request;
import com.buddy.api.web.pets.v2.requests.UpdatePetV2Request;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@DisplayName("PetV2Controller — Integration Tests (/v2/pets)")
class PetV2ControllerTest extends IntegrationTestAbstract {

    private static final String BASE_URL = "/v2/pets";

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CreatePetV2 createPetV2;

    @Autowired
    private UpdatePetV2 updatePetV2;

    private AccountEntity shelterAccount;
    private ProfileEntity shelterProfile;
    private String shelterToken;

    private AccountEntity otherAccount;
    private String otherToken;

    @BeforeEach
    void setUp() {
        imageRepository.deleteAll();
        petV2Repository.deleteAll();

        shelterAccount = accountRepository.save(
            validAccountEntity()
                .email(new EmailAddress("shelter-" + UUID.randomUUID() + "@buddy.com"))
                .isVerified(true)
                .build()
        );

        shelterProfile = profileRepository.save(
            ProfileEntity.builder()
                .account(shelterAccount)
                .name("Shelter One")
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build()
        );

        shelterToken = jwtUtil.generateAccessToken(
            shelterAccount.getEmail().value(),
            List.of("ROLE_SHELTER", "SCOPE_VERIFIED")
        );

        otherAccount = accountRepository.save(
            validAccountEntity()
                .email(new EmailAddress("other-" + UUID.randomUUID() + "@buddy.com"))
                .isVerified(true)
                .build()
        );

        profileRepository.save(
            ProfileEntity.builder()
                .account(otherAccount)
                .name("Other Shelter")
                .profileType(ProfileTypeEnum.SHELTER)
                .isDeleted(false)
                .build()
        );

        otherToken = jwtUtil.generateAccessToken(
            otherAccount.getEmail().value(),
            List.of("ROLE_SHELTER", "SCOPE_VERIFIED")
        );
    }

    @Nested
    @DisplayName("POST /v2/pets/register")
    class RegisterPetTests {

        @Test
        @DisplayName("Should return 201 Created and persist PetV2 linked to authenticated guardian")
        void should_register_pet_successfully() throws Exception {
            final var request = CreatePetV2Request.builder()
                .name("Zeus")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(3)
                .size(BigDecimal.valueOf(55.0))
                .weight(BigDecimal.valueOf(22.5))
                .isNeutered(true)
                .isForAdoption(true)
                .description("Friendly pet")
                .build();

            mockMvc.perform(post(BASE_URL + "/register")
                    .header(AUTHORIZATION, BEARER + shelterToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            final var pets = petV2Repository.findAll();
            assertThat(pets).hasSize(1);
            assertThat(pets.get(0).getName()).isEqualTo("Zeus");
            assertThat(pets.get(0).getGuardianProfile().getProfileId())
                .isEqualTo(shelterProfile.getProfileId());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when no token is provided")
        void should_return_403_when_unauthenticated() throws Exception {
            final var request = CreatePetV2Request.builder()
                .name("Zeus")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .build();

            mockMvc.perform(post(BASE_URL + "/register")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when account has only USER role")
        void should_return_403_when_not_shelter_role() throws Exception {
            final var userAccount = accountRepository.save(
                validAccountEntity()
                    .email(new EmailAddress("user-" + UUID.randomUUID() + "@buddy.com"))
                    .isVerified(true)
                    .build()
            );
            profileRepository.save(
                ProfileEntity.builder()
                    .account(userAccount)
                    .name("Regular User")
                    .profileType(ProfileTypeEnum.USER)
                    .isDeleted(false)
                    .build()
            );
            final var userToken = jwtUtil.generateAccessToken(
                userAccount.getEmail().value(),
                List.of("ROLE_USER", "SCOPE_VERIFIED")
            );
            final var request = CreatePetV2Request.builder()
                .name("Zeus")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .build();

            mockMvc.perform(post(BASE_URL + "/register")
                    .header(AUTHORIZATION, BEARER + userToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when shelter account is not verified")
        void should_return_403_when_not_verified() throws Exception {
            final var unverifiedAccount = accountRepository.save(
                validAccountEntity()
                    .email(new EmailAddress("unverified-" + UUID.randomUUID() + "@buddy.com"))
                    .isVerified(false)
                    .build()
            );
            profileRepository.save(
                ProfileEntity.builder()
                    .account(unverifiedAccount)
                    .name("Unverified Shelter")
                    .profileType(ProfileTypeEnum.SHELTER)
                    .isDeleted(false)
                    .build()
            );
            final var unverifiedToken = jwtUtil.generateAccessToken(
                unverifiedAccount.getEmail().value(),
                List.of("ROLE_SHELTER")
            );
            final var request = CreatePetV2Request.builder()
                .name("Zeus")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .build();

            mockMvc.perform(post(BASE_URL + "/register")
                    .header(AUTHORIZATION, BEARER + unverifiedToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should enforce active shelter profile validation (422) on domain creation")
        void should_throw_422_when_active_shelter_profile_missing() {
            final var noShelterAccount = accountRepository.save(
                validAccountEntity()
                    .email(new EmailAddress("noshelter-" + UUID.randomUUID() + "@buddy.com"))
                    .isVerified(true)
                    .build()
            );
            profileRepository.save(
                ProfileEntity.builder()
                    .account(noShelterAccount)
                    .name("Deleted Shelter")
                    .profileType(ProfileTypeEnum.SHELTER)
                    .isDeleted(true)
                    .build()
            );

            final var dto = CreatePetV2Dto.builder()
                .accountId(noShelterAccount.getAccountId())
                .name("Zeus")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build();

            assertThatThrownBy(() -> createPetV2.create(dto))
                .isInstanceOf(ActiveShelterProfileNotFoundException.class)
                .satisfies(ex -> {
                    final var domainEx = (ActiveShelterProfileNotFoundException) ex;
                    assertThat(domainEx.getHttpStatus()).isEqualTo(HttpStatus.resolve(422));
                    assertThat(domainEx.getFieldName()).isEqualTo("profile");
                });
        }

        @Test
        @DisplayName("Should return 400 Bad Request on invalid payload")
        void should_return_400_on_invalid_payload() throws Exception {
            final var request = CreatePetV2Request.builder()
                .name("")
                .species(null)
                .gender(null)
                .build();

            mockMvc.perform(post(BASE_URL + "/register")
                    .header(AUTHORIZATION, BEARER + shelterToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 429 Too Many Requests when registration rate limit is exceeded")
        void should_return_429_when_register_rate_limit_exceeded() throws Exception {
            final var request = CreatePetV2Request.builder()
                .name("Zeus")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .build();
            final var body = objectMapper.writeValueAsString(request);

            for (int attempt = 0; attempt < 5; attempt++) {
                mockMvc.perform(post(BASE_URL + "/register")
                        .header(AUTHORIZATION, BEARER + shelterToken)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isCreated());
            }

            mockMvc.perform(post(BASE_URL + "/register")
                    .header(AUTHORIZATION, BEARER + shelterToken)
                    .contentType(APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    @DisplayName("GET /v2/pets (Public Search)")
    class SearchPetsTests {

        @Test
        @DisplayName("Should return 200 OK with PageResponse publicly without token")
        void should_search_pets_publicly() throws Exception {
            petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Thor")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Mimi")
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .isForAdoption(true)
                .build());

            mockMvc.perform(get(BASE_URL)
                    .param("species", "DOG")
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Thor"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("Should return all pets when isForAdoption omitted and filter when specified")
        void should_return_all_pets_when_is_for_adoption_omitted() throws Exception {
            petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Available Dog")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Adopted Dog")
                .species(PetSpecies.DOG)
                .gender(PetGender.FEMALE)
                .isForAdoption(false)
                .build());

            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

            mockMvc.perform(get(BASE_URL).param("isForAdoption", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Available Dog"));

            mockMvc.perform(get(BASE_URL).param("isForAdoption", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Adopted Dog"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when search criteria is invalid")
        void should_return_400_when_search_criteria_invalid() throws Exception {
            mockMvc.perform(get(BASE_URL)
                    .param("minSize", "50")
                    .param("maxSize", "10"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /v2/pets/{id} (Public Details)")
    class GetPetDetailsTests {

        @Test
        @DisplayName("Should return 200 OK with full pet details publicly")
        void should_get_pet_details() throws Exception {
            final var pet = petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Apollo")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .approximateAge(2)
                .ageReportDate(LocalDate.now())
                .isForAdoption(true)
                .description("Playful puppy")
                .build());

            imageRepository.save(ImageEntity.builder()
                .petV2(pet)
                .filePath("http://cdn.buddy.com/apollo.jpg")
                .displayOrder(0)
                .isAvatar(true)
                .build());

            mockMvc.perform(get(BASE_URL + "/" + pet.getPetV2Id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pet.getPetV2Id().toString()))
                .andExpect(jsonPath("$.name").value("Apollo"))
                .andExpect(jsonPath("$.coverImageUrl").value("http://cdn.buddy.com/apollo.jpg"))
                .andExpect(jsonPath("$.images.length()").value(1));
        }

        @Test
        @DisplayName("Should return 404 Not Found when pet does not exist")
        void should_return_404_when_pet_not_found() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /v2/pets/{id}")
    class UpdatePetTests {

        @Test
        @DisplayName("Should return 200 OK and update pet when guardian owner updates it")
        void should_update_pet_when_owner() throws Exception {
            final var pet = petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Rex")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            final var request = UpdatePetV2Request.builder()
                .name("Rex Updated")
                .weight(BigDecimal.valueOf(18.0))
                .isForAdoption(false)
                .build();

            mockMvc.perform(put(BASE_URL + "/" + pet.getPetV2Id())
                    .header(AUTHORIZATION, BEARER + shelterToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rex Updated"))
                .andExpect(jsonPath("$.isForAdoption").value(false));

            final var updated = petV2Repository.findById(pet.getPetV2Id()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Rex Updated");
            assertThat(updated.getIsForAdoption()).isFalse();
        }

        @Test
        @DisplayName("Should return 200 OK when verified ADMIN without shelter profile updates pet")
        void should_update_pet_when_admin() throws Exception {
            final var adminAccount = accountRepository.save(
                validAccountEntity()
                    .email(new EmailAddress("admin-" + UUID.randomUUID() + "@buddy.com"))
                    .isVerified(true)
                    .build()
            );
            profileRepository.save(
                ProfileEntity.builder()
                    .account(adminAccount)
                    .name("Platform Admin")
                    .profileType(ProfileTypeEnum.ADMIN)
                    .isDeleted(false)
                    .build()
            );
            final var adminToken = jwtUtil.generateAccessToken(
                adminAccount.getEmail().value(),
                List.of("ROLE_ADMIN", "SCOPE_VERIFIED")
            );

            final var pet = petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Rex")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            final var request = UpdatePetV2Request.builder()
                .name("Rex Admin Updated")
                .build();

            mockMvc.perform(put(BASE_URL + "/" + pet.getPetV2Id())
                    .header(AUTHORIZATION, BEARER + adminToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rex Admin Updated"));

            final var updated = petV2Repository.findById(pet.getPetV2Id()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Rex Admin Updated");
        }

        @Test
        @DisplayName("Should return 403 Forbidden when another shelter attempts to update the pet")
        void should_return_403_when_not_owner() throws Exception {
            final var pet = petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Rex")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            final var request = UpdatePetV2Request.builder()
                .name("Hacked Name")
                .build();

            mockMvc.perform(put(BASE_URL + "/" + pet.getPetV2Id())
                    .header(AUTHORIZATION, BEARER + otherToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when updating without token")
        void should_return_403_when_unauthenticated() throws Exception {
            final var request = UpdatePetV2Request.builder()
                .name("Rex Updated")
                .build();

            mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID())
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when update payload is invalid")
        void should_return_400_on_invalid_update_payload() throws Exception {
            final var pet = petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Rex")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            final var request = UpdatePetV2Request.builder()
                .name("   ")
                .build();

            mockMvc.perform(put(BASE_URL + "/" + pet.getPetV2Id())
                    .header(AUTHORIZATION, BEARER + shelterToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existent pet")
        void should_return_404_when_updating_non_existent_pet() throws Exception {
            final var request = UpdatePetV2Request.builder()
                .name("Ghost Pet")
                .build();

            mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID())
                    .header(AUTHORIZATION, BEARER + shelterToken)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should enforce active shelter profile validation (422) on domain update")
        void should_throw_422_when_updating_without_active_shelter_profile() {
            final var pet = petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Rex")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            final var noShelterAccount = accountRepository.save(
                validAccountEntity()
                    .email(new EmailAddress("noshelter-upd-" + UUID.randomUUID() + "@buddy.com"))
                    .isVerified(true)
                    .build()
            );

            final var dto = UpdatePetV2Dto.builder()
                .id(pet.getPetV2Id())
                .name("Updated Name")
                .build();

            assertThatThrownBy(() ->
                updatePetV2.update(dto, noShelterAccount.getAccountId(), false))
                .isInstanceOf(ActiveShelterProfileNotFoundException.class)
                .satisfies(ex -> {
                    final var domainEx = (ActiveShelterProfileNotFoundException) ex;
                    assertThat(domainEx.getHttpStatus()).isEqualTo(HttpStatus.resolve(422));
                    assertThat(domainEx.getFieldName()).isEqualTo("profile");
                });
        }

        @Test
        @DisplayName("Should return 429 Too Many Requests when update rate limit is exceeded")
        void should_return_429_when_update_rate_limit_exceeded() throws Exception {
            final var pet = petV2Repository.save(PetV2Entity.builder()
                .guardianProfile(shelterProfile)
                .name("Rex")
                .species(PetSpecies.DOG)
                .gender(PetGender.MALE)
                .isForAdoption(true)
                .build());

            final var request = UpdatePetV2Request.builder()
                .name("Rex Updated")
                .build();
            final var body = objectMapper.writeValueAsString(request);

            for (int attempt = 0; attempt < 5; attempt++) {
                mockMvc.perform(put(BASE_URL + "/" + pet.getPetV2Id())
                        .header(AUTHORIZATION, BEARER + shelterToken)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                    .andExpect(status().isOk());
            }

            mockMvc.perform(put(BASE_URL + "/" + pet.getPetV2Id())
                    .header(AUTHORIZATION, BEARER + shelterToken)
                    .contentType(APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isTooManyRequests());
        }
    }
}
