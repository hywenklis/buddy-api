package com.buddy.api.integrations.web.pet.controller;

import static com.buddy.api.builders.pet.PetBuilder.createPetRequest;
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
import org.springframework.http.HttpStatus;

@DisplayName("POST /v1/pets/register")
class CreatePetControllerTest extends IntegrationTestAbstract {

    private static final String PET_REGISTER_URL = "/v1/pets/register";
    private static final String ERROR_FIELD_PATH = "$.errors[0].field";
    private static final String ERROR_MESSAGE_PATH = "$.errors[0].message";
    private static final String ERROR_HTTP_STATUS_PATH = "$.errors[0].httpStatus";
    private static final String ERROR_CODE_PATH = "$.errors[0].errorCode";
    private static final String ERROR_TIMESTAMP_PATH = "$.errors[0].timestamp";

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
    @DisplayName("Should return not found if there is no shelter id passed in the request")
    void should_return_not_found() throws Exception {
        var request = createPetRequest(UUID.randomUUID());

        mockMvc
                .perform(post(PET_REGISTER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_FIELD_PATH).value("shelterId"))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Shelter not found"))
                .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value("NOT_FOUND"))
                .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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

        mockMvc
                .perform(post(PET_REGISTER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD_PATH).value("name"))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Name of mandatory pet"))
                .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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

        mockMvc
                .perform(post(PET_REGISTER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD_PATH).value("specie"))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Specie of mandatory pet"))
                .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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

        mockMvc
                .perform(post(PET_REGISTER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD_PATH).value("gender"))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Gender of mandatory pet"))
                .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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

        mockMvc
                .perform(post(PET_REGISTER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD_PATH).value("weight"))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Weight of mandatory pet"))
                .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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

        mockMvc
                .perform(post(PET_REGISTER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD_PATH).value("description"))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Description of mandatory pet"))
                .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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

        mockMvc
                .perform(post(PET_REGISTER_URL)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD_PATH).value("shelterId"))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("ShelterId of mandatory pet"))
                .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
    }
}
