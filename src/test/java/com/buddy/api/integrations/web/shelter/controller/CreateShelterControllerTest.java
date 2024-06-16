package com.buddy.api.integrations.web.shelter.controller;

import static com.buddy.api.builders.shelter.ShelterBuilder.createShelterRequest;
import static com.buddy.api.utils.RandomCpfUtils.generateValidCpf;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.integrations.IntegrationTestAbstract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("POST /v1/shelters/register")
class CreateShelterControllerTest extends IntegrationTestAbstract {

    @Test
    @DisplayName("Should register a new shelter successfully")
    void register_new_shelter_success() throws Exception {
        var request = createShelterRequest();

        mockMvc
                .perform(post("/v1/shelters/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("successfully created"));
    }

    @Test
    @DisplayName("Should return bad request if there is already "
            + " a cpf registered in the database")
    void should_return_bad_request_cpf_registered_db() throws Exception {
        var request = createShelterRequest();

        shelterComponent.createShelter(
                request.nameShelter(),
                request.nameResponsible(),
                request.cpfResponsible(),
                null,
                null,
                null,
                request.avatar(),
                null
        );

        mockMvc
                .perform(post("/v1/shelters/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("cpfResponsible"))
                .andExpect(jsonPath("$.errors[0].message").value("CPF must be unique"))
                .andExpect(jsonPath("$.errors[0].httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors[0].timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if there is already "
            + " a email registered in the database")
    void should_return_bad_request_email_registered_db() throws Exception {
        var request = createShelterRequest();

        shelterComponent.createShelter(
                request.nameShelter(),
                request.nameResponsible(),
                null,
                request.email(),
                null,
                null,
                request.avatar(),
                null
        );

        mockMvc
                .perform(post("/v1/shelters/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("EMAIL must be unique"))
                .andExpect(jsonPath("$.errors[0].httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors[0].timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if nameShelter is not filled in")
    void should_return_bad_request_name_shelter_not_filled() throws Exception {
        var request = createShelterRequest(
                null,
                randomAlphabetic(10),
                generateValidCpf(),
                generateValidEmail(),
                randomAlphabetic(10)
        );

        mockMvc
                .perform(post("/v1/shelters/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("nameShelter"))
                .andExpect(jsonPath("$.errors[0].message").value("Name of mandatory shelter"))
                .andExpect(jsonPath("$.errors[0].httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors[0].timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if nameResponsible is not filled in")
    void should_return_bad_request_name_responsible_not_filled() throws Exception {
        var request = createShelterRequest(
                randomAlphabetic(10),
                null,
                generateValidCpf(),
                generateValidEmail(),
                randomAlphabetic(10)
        );

        mockMvc
                .perform(post("/v1/shelters/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("nameResponsible"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Responsible for the mandatory shelter"))
                .andExpect(jsonPath("$.errors[0].httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors[0].timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if cpfResponsible is not filled in")
    void should_return_bad_request_cpf_responsible_not_filled() throws Exception {
        var request = createShelterRequest(
                randomAlphabetic(10),
                randomAlphabetic(10),
                null,
                generateValidEmail(),
                randomAlphabetic(10)
        );

        mockMvc
                .perform(post("/v1/shelters/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("cpfResponsible"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Mandatory responsible person's CPF"))
                .andExpect(jsonPath("$.errors[0].httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors[0].timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if email is not filled in")
    void should_return_bad_request_email_not_filled() throws Exception {
        var request = createShelterRequest(
                randomAlphabetic(10),
                randomAlphabetic(10),
                generateValidCpf(),
                null,
                randomAlphabetic(10)
        );

        mockMvc
                .perform(post("/v1/shelters/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Mandatory EMAIL"))
                .andExpect(jsonPath("$.errors[0].httpStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors[0].timestamp").isNotEmpty());
    }
}
