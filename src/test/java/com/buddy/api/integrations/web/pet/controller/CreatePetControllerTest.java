package com.buddy.api.integrations.web.pet.controller;

import static com.buddy.api.builders.pet.PetBuilder.createPetRequest;
import static com.buddy.api.utils.RandomCpfUtils.generateValidCpf;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.builders.pet.PetImageBuilder;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("POST /v1/pets/register")
class CreatePetControllerTest extends IntegrationTestAbstract {

    @Test
    @DisplayName("Should register a new pet successfully")
    void register_new_pet_success() throws Exception {
        var images = PetImageBuilder.createPetRequest(randomAlphabetic(10));

        var shelter = shelterComponent.createShelter(
                randomAlphabetic(10),
                randomAlphabetic(10),
                generateValidCpf(),
                generateValidEmail(),
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                null
        );

        var request = createPetRequest(
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                Integer.valueOf(randomNumeric(1)),
                Double.valueOf(randomNumeric(1)),
                randomAlphabetic(10),
                randomAlphabetic(10),
                List.of(images),
                shelter.getId()
        );

        mockMvc
                .perform(post("/v1/pets/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("successfully created"));
    }

    @Test
    @DisplayName("Should return not found return not found "
            + "if there is no shelter id passed in the request")
    void should_return_not_found() throws Exception {
        var images = PetImageBuilder.createPetRequest(randomAlphabetic(10));

        var request = createPetRequest(
                randomAlphabetic(10),
                randomAlphabetic(10),
                randomAlphabetic(10),
                Integer.valueOf(randomNumeric(1)),
                Double.valueOf(randomNumeric(1)),
                randomAlphabetic(10),
                randomAlphabetic(10),
                List.of(images),
                UUID.randomUUID()
        );

        mockMvc
                .perform(post("/v1/pets/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].field").value("shelterId"))
                .andExpect(jsonPath("$.errors[0].message").value("Shelter not found"))
                .andExpect(jsonPath("$.errors[0].httpStatus").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].errorCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errors[0].timestamp").isNotEmpty());
    }
}
