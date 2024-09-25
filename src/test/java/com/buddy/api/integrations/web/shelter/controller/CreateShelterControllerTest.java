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

    private static final String SHELTER_REGISTER_URL = "/v1/shelters/register";
    private static final String ERROR_FIELD_PATH = "$.errors[0].field";
    private static final String ERROR_MESSAGE_PATH = "$.errors[0].message";
    private static final String ERROR_HTTP_STATUS_PATH = "$.errors[0].httpStatus";
    private static final String ERROR_CODE_PATH = "$.errors[0].errorCode";
    private static final String ERROR_TIMESTAMP_PATH = "$.errors[0].timestamp";

    @Test
    @DisplayName("Should register a new shelter successfully")
    void register_new_shelter_success() throws Exception {
        var request = createShelterRequest();

        mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("successfully created"));
    }

    @Test
    @DisplayName("Should return bad request if there is already a cpf registered in the database")
    void should_return_bad_request_cpf_registered_db() throws Exception {
        var request = createShelterRequest();

        shelterComponent.createShelter(
            request.nameShelter(),
            request.nameResponsible(),
            request.cpfResponsible(),
            generateValidEmail(),
            null,
            null,
            request.avatar(),
            null
        );

        mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH).value("cpfResponsible"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("CPF must be unique"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if there is already "
        + "an email registered in the database")
    void should_return_bad_request_email_registered_db() throws Exception {
        var request = createShelterRequest();

        shelterComponent.createShelter(
            request.nameShelter(),
            request.nameResponsible(),
            generateValidCpf(),
            request.email(),
            null,
            null,
            request.avatar(),
            null
        );

        mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH).value("email"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Email must be unique"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH).value("nameShelter"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Shelter name is mandatory"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH)
                .value("nameResponsible"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH)
                .value("Responsible person's name is mandatory"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH).value("cpfResponsible"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Responsible person's CPF is mandatory"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if cpfResponsible is invalid format")
    void should_return_bad_request_cpf_responsible_is_invalid_format() throws Exception {
        var request = createShelterRequest(
            randomAlphabetic(10),
            randomAlphabetic(10),
            randomAlphabetic(10),
            generateValidEmail(),
            randomAlphabetic(10)
        );

        mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH).value("cpfResponsible"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Invalid CPF format"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
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
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH).value("email"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Email is mandatory"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
    }

    @Test
    @DisplayName("Should return bad request if email is invalid format")
    void should_return_bad_request_email_is_invalid_format() throws Exception {
        var request = createShelterRequest(
            randomAlphabetic(10),
            randomAlphabetic(10),
            generateValidCpf(),
            randomAlphabetic(10),
            randomAlphabetic(10)
        );

        mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_FIELD_PATH).value("email"))
            .andExpect(jsonPath(ERROR_MESSAGE_PATH).value("Invalid email format"))
            .andExpect(jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty());
    }
}
