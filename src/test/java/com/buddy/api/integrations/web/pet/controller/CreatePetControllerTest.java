package com.buddy.api.integrations.web.pet.controller;

import static com.buddy.api.builders.pet.PetBuilder.createPetRequest;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("POST /v1/pets/register")
class CreatePetControllerTest extends IntegrationTestAbstract {

    @Test
    @DisplayName("Should register a new pet successfully")
    void register_new_pet_success() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();
        var request = createPetRequest(shelter.getId());

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
        var request = createPetRequest(UUID.randomUUID());

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
