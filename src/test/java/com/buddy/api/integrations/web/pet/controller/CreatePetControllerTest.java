package com.buddy.api.integrations.web.pet.controller;

import static com.buddy.api.builders.pet.PetBuilder.createPetRequest;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.pet.PetBuilder;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("POST /v1/pets/register")
class CreatePetControllerTest extends IntegrationTestAbstract {

    private static final String PET_REGISTER_URL = "/v1/pets/register";

    @Test
    @DisplayName("Should register a new pet successfully")
    void register_new_pet_success() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();
        var request = createPetRequest(shelter.getId());

        mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("successfully created"));
    }

    @Test
    @DisplayName("Should return not found if the shelter id does not exists in the database")
    void should_return_not_found_shelterId_not_exists() throws Exception {
        var request = createPetRequest(UUID.randomUUID());

        expectNotFoundFrom(mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("shelterId", "Shelter not found");
    }

    @Test
    @DisplayName("Should return bad request if petName is not filled in")
    void should_return_bad_request_pet_name_not_filled() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var request = PetBuilder.createPetRequest(
            null,
            randomAlphabetic(10),
            randomAlphabetic(10),
            Double.valueOf(randomNumeric(1)),
            randomAlphabetic(10),
            randomAlphabetic(10),
            List.of(),
            shelter.getId()
        );

        expectBadRequestFrom(mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("name", "Pet name is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if specie name is not filled in")
    void should_return_bad_request_specie_name_not_filled() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var request = PetBuilder.createPetRequest(
            randomAlphabetic(10),
            null,
            randomAlphabetic(10),
            Double.valueOf(randomNumeric(1)),
            randomAlphabetic(10),
            randomAlphabetic(10),
            List.of(),
            shelter.getId()
        );

        expectBadRequestFrom(mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("specie", "Pet species is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if gender name is not filled in")
    void should_return_bad_request_sex_name_not_filled() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var request = PetBuilder.createPetRequest(
            randomAlphabetic(10),
            randomAlphabetic(10),
            null,
            Double.valueOf(randomNumeric(1)),
            randomAlphabetic(10),
            randomAlphabetic(10),
            List.of(),
            shelter.getId()
        );

        expectBadRequestFrom(mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("gender", "Pet gender is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if weight is not filled in")
    void should_return_bad_request_weight_not_filled() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var request = PetBuilder.createPetRequest(
            randomAlphabetic(10),
            randomAlphabetic(10),
            randomAlphabetic(10),
            null,
            randomAlphabetic(10),
            randomAlphabetic(10),
            List.of(),
            shelter.getId()
        );

        expectBadRequestFrom(mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("weight", "Pet weight is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if description is not filled in")
    void should_return_bad_request_description_not_filled() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var request = PetBuilder.createPetRequest(
            randomAlphabetic(10),
            randomAlphabetic(10),
            randomAlphabetic(10),
            Double.valueOf(randomNumeric(1)),
            null,
            randomAlphabetic(10),
            List.of(),
            shelter.getId()
        );

        expectBadRequestFrom(mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("description", "Pet description is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if shelterId is not filled in")
    void should_return_bad_request_shelterId_not_filled() throws Exception {
        var request = PetBuilder.createPetRequest(
            randomAlphabetic(10),
            randomAlphabetic(10),
            randomAlphabetic(10),
            Double.valueOf(randomNumeric(1)),
            randomAlphabetic(10),
            randomAlphabetic(10),
            List.of(),
            null
        );

        expectBadRequestFrom(mockMvc
            .perform(post(PET_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("shelterId", "Shelter ID is mandatory");
    }
}
