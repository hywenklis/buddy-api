package com.buddy.api.integrations.web.shelter.controller;

import static com.buddy.api.builders.shelter.ShelterBuilder.createShelterRequest;
import static com.buddy.api.customverifications.CustomCreatedVerifications.expectCreatedFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.utils.RandomCpfUtils.generateValidCpf;
import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.buddy.api.integrations.IntegrationTestAbstract;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("POST /v1/shelters/register")
class CreateShelterControllerTest extends IntegrationTestAbstract {

    private static final String SHELTER_REGISTER_URL = "/v1/shelters/register";

    @Test
    @DisplayName("Should register a new shelter successfully")
    void register_new_shelter_success() throws Exception {
        var request = createShelterRequest();

        expectCreatedFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
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

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("cpfResponsible", "CPF must be unique");
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

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("email", "Email must be unique");
    }

    @Test
    @DisplayName("Should return bad request if nameShelter is not filled in")
    void should_return_bad_request_name_shelter_not_filled() throws Exception {
        var request = createShelterRequest(
            null,
            RandomStringUtils.secure().nextAlphabetic(10),
            generateValidCpf(),
            generateValidEmail(),
            RandomStringUtils.secure().nextAlphabetic(10)
        );

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("nameShelter", "Shelter name is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if nameResponsible is not filled in")
    void should_return_bad_request_name_responsible_not_filled() throws Exception {
        var request = createShelterRequest(
            RandomStringUtils.secure().nextAlphabetic(10),
            null,
            generateValidCpf(),
            generateValidEmail(),
            RandomStringUtils.secure().nextAlphabetic(10)
        );

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("nameResponsible", "Responsible person's name is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if cpfResponsible is not filled in")
    void should_return_bad_request_cpf_responsible_not_filled() throws Exception {
        var request = createShelterRequest(
            RandomStringUtils.secure().nextAlphabetic(10),
            RandomStringUtils.secure().nextAlphabetic(10),
            null,
            generateValidEmail(),
            RandomStringUtils.secure().nextAlphabetic(10)
        );

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("cpfResponsible", "Responsible person's CPF is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if cpfResponsible is invalid format")
    void should_return_bad_request_cpf_responsible_is_invalid_format() throws Exception {
        var request = createShelterRequest(
            RandomStringUtils.secure().nextAlphabetic(10),
            RandomStringUtils.secure().nextAlphabetic(10),
            RandomStringUtils.secure().nextAlphabetic(10),
            generateValidEmail(),
            RandomStringUtils.secure().nextAlphabetic(10)
        );

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("cpfResponsible", "Invalid CPF format");
    }

    @Test
    @DisplayName("Should return bad request if email is not filled in")
    void should_return_bad_request_email_not_filled() throws Exception {
        var request = createShelterRequest(
            RandomStringUtils.secure().nextAlphabetic(10),
            RandomStringUtils.secure().nextAlphabetic(10),
            generateValidCpf(),
            null,
            RandomStringUtils.secure().nextAlphabetic(10)
        );

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("email", "Email is mandatory");
    }

    @Test
    @DisplayName("Should return bad request if email is invalid format")
    void should_return_bad_request_email_is_invalid_format() throws Exception {
        var request = createShelterRequest(
            RandomStringUtils.secure().nextAlphabetic(10),
            RandomStringUtils.secure().nextAlphabetic(10),
            generateValidCpf(),
            RandomStringUtils.secure().nextAlphabetic(10),
            RandomStringUtils.secure().nextAlphabetic(10)
        );

        expectBadRequestFrom(mockMvc
            .perform(post(SHELTER_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))))
            .forField("email", "Invalid email format");
    }
}
