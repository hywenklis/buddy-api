package com.buddy.api.integrations.web.pet.controller;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("GET /v1/pets")
class FindPetControllerTest extends IntegrationTestAbstract {
    private static final String ERROR_FIELD_PATH = "$.errors[0].field";
    private static final String ERROR_MESSAGE_PATH = "$.errors[0].message";
    private static final String ERROR_HTTP_STATUS_PATH = "$.errors[0].httpStatus";
    private static final String ERROR_CODE_PATH = "$.errors[0].errorCode";
    private static final String ERROR_TIMESTAMP_PATH = "$.errors[0].timestamp";

    @BeforeEach
    void setUp() {
        shelter = shelterComponent.createShelterNoPets();
    }

    @Test
    @DisplayName("Should return last two pets ordered by createDate descending")
    void return_last_two_pets_ordered_createDate_desc() throws Exception {
        petComponent.savePetWithName(randomAlphabetic(4), shelter);
        PetEntity lolo = petComponent.savePetWithName(randomAlphabetic(4), shelter);
        PetEntity kiki = petComponent.savePetWithName(randomAlphabetic(4), shelter);

        performGetRequestAndExpectTwoPets(
            PET_BASE_URL + "?page=0&size=2&sort=createDate,desc",
            kiki,
            lolo
        );
    }

    @Test
    @DisplayName("Should return pets ordered by createDate descending by default")
    void return_pets_ordered_createDate_desc_default() throws Exception {
        clearRepositories();
        PetEntity lolo = petComponent.savePetWithName(randomAlphabetic(4), shelter);
        PetEntity kiki = petComponent.savePetWithName(randomAlphabetic(4), shelter);

        performGetRequestAndExpectTwoPets(PET_BASE_URL, kiki, lolo);
    }

    @Test
    @DisplayName("Should return first two pets ordered by createDate ascending")
    void return_first_two_pets_ordered_createDate_asc() throws Exception {
        clearRepositories();

        PetEntity tata = petComponent.savePetWithName(randomAlphabetic(4), shelter);
        PetEntity lolo = petComponent.savePetWithName(randomAlphabetic(4), shelter);
        petComponent.savePetWithName(randomAlphabetic(4), shelter);

        performGetRequestAndExpectTwoPets(
            PET_BASE_URL + "?page=0&size=2&sort=createDate,asc",
            tata,
            lolo
        );
    }

    @Test
    @DisplayName("Should correctly paginate when only page and size are provided")
    void return_all_pets_when_no_order_defined() throws Exception {
        petComponent.savePetWithName(randomAlphabetic(4), shelter);
        petComponent.savePetWithName(randomAlphabetic(4), shelter);
        PetEntity kiki = petComponent.savePetWithName(randomAlphabetic(4), shelter);
        PetEntity zeze = petComponent.savePetWithName(randomAlphabetic(4), shelter);

        performGetRequestAndExpectTwoPets(PET_BASE_URL + "?page=0&size=2", zeze, kiki);
    }

    @Test
    @DisplayName("Should return pet by id")
    void return_pet_by_id() throws Exception {
        PetEntity pet = petComponent.savePetWithName(randomAlphabetic(4), shelter);

        mockMvc.perform(get(PET_BASE_URL + "?id=" + pet.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").exists())
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES, isA(List.class)))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES, hasSize(1)))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[0].id",
                equalTo(pet.getId().toString())))
            .andExpect(
                jsonPath(EMBEDDED_PET_RESPONSES + "[0].name", equalTo(pet.getName())))
            .andExpect(
                jsonPath(EMBEDDED_PET_RESPONSES + "[0].gender", equalTo(pet.getGender())))
            .andExpect(
                jsonPath(EMBEDDED_PET_RESPONSES + "[0].specie", equalTo(pet.getSpecie())))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[0].location",
                equalTo(pet.getLocation())))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[0].birthDate",
                equalTo(pet.getBirthDate().toString())))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[0].description",
                equalTo(pet.getDescription())))
            .andExpect(
                jsonPath(EMBEDDED_PET_RESPONSES + "[0].weight", equalTo(pet.getWeight())))
            .andExpect(
                jsonPath(EMBEDDED_PET_RESPONSES + "[0].avatar", equalTo(pet.getAvatar())))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[0].images", hasSize(0)))
            .andExpect(
                jsonPath(EMBEDDED_PET_RESPONSES + "[0].shelterResponseCompact.nameShelter",
                    equalTo(shelter.getNameShelter())))
            .andExpect(
                jsonPath(EMBEDDED_PET_RESPONSES + "[0].shelterResponseCompact.avatar",
                    equalTo(shelter.getAvatar())));
    }

    @Test
    @DisplayName("Should return pets with given name")
    void return_pet_by_name() throws Exception {
        String petName = randomAlphabetic(4);
        PetEntity firstTata = petComponent.savePetWithName(petName, shelter);
        petComponent.savePetWithName(randomAlphabetic(4), shelter);
        PetEntity secondTata = petComponent.savePetWithName(petName, shelter);

        performGetRequestAndExpectTwoPets(
            PET_BASE_URL + "?name=" + firstTata.getName() + "&page=0&size=2",
            secondTata,
            firstTata
        );
    }

    @Test
    @DisplayName("Should return pets with age between 0 and 1 years including")
    void return_pet_by_ageRange_0_to_1_years() throws Exception {
        PetEntity zeroYearsPet = petComponent.savePetWithBirthDate(
            LocalDate.now().minusMonths(3), shelter
        );
        PetEntity almostOneYearPet = petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(1).plusDays(1), shelter
        );
        petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(1).minusDays(1), shelter
        );

        assertThat(almostOneYearPet.getCreateDate()).isAfter(zeroYearsPet.getCreateDate());

        performGetRequestAndExpectTwoPetsInOrder(
            PET_BASE_URL + "?ageRange=0-1 anos",
            almostOneYearPet,
            zeroYearsPet
        );
    }

    @Test
    @DisplayName("Should return pets with 1 <= age < 2")
    void return_pet_by_ageRange_1_to_2_years() throws Exception {
        petComponent.savePetWithBirthDate(LocalDate.now().minusMonths(3), shelter);
        PetEntity oneYearPet = petComponent.savePetWithAge(1, shelter);
        PetEntity almostTwoYearsPet = petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(2).plusDays(1), shelter
        );
        petComponent.savePetWithAge(2, shelter);

        performGetRequestAndExpectTwoPetsInOrder(
            PET_BASE_URL + "?ageRange=1-2 anos",
            almostTwoYearsPet,
            oneYearPet
        );
    }

    @Test
    @DisplayName("Should return pets with 2 <= age < 3")
    void return_pet_by_ageRange_2_to_3_years() throws Exception {
        petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(2).plusDays(1), shelter
        );
        PetEntity twoYearsPet = petComponent.savePetWithAge(2, shelter);
        PetEntity almostThreeYearsPet = petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(3).plusDays(1), shelter
        );
        petComponent.savePetWithAge(3, shelter);

        performGetRequestAndExpectTwoPetsInOrder(PET_BASE_URL + "?ageRange=2-3 anos",
            almostThreeYearsPet, twoYearsPet);
    }

    @Test
    @DisplayName("Should return pets with 3 <= age < 5")
    void return_pet_by_ageRange_3_to_5_years() throws Exception {
        petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(3).plusDays(1), shelter
        );
        PetEntity threeYearsPet = petComponent.savePetWithAge(3, shelter);
        PetEntity almostFiveYearsPet = petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(5).plusDays(1), shelter
        );
        petComponent.savePetWithAge(5, shelter);

        performGetRequestAndExpectTwoPetsInOrder(PET_BASE_URL + "?ageRange=3-5 anos",
            almostFiveYearsPet, threeYearsPet);
    }

    @Test
    @DisplayName("Should return pets with 5 <= age 10")
    void return_pet_by_ageRange_5_to_10_years() throws Exception {
        petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(5).plusDays(1), shelter
        );
        PetEntity fiveYearsPet = petComponent.savePetWithAge(5, shelter);
        PetEntity almostTenYearsPet = petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(10).plusDays(1), shelter
        );
        petComponent.savePetWithAge(10, shelter);

        performGetRequestAndExpectTwoPetsInOrder(PET_BASE_URL + "?ageRange=5-10 anos",
            almostTenYearsPet, fiveYearsPet);
    }

    @Test
    @DisplayName("Should return pets with 10 <= age")
    void return_pet_by_ageRange_10_plus_years() throws Exception {
        petComponent.savePetWithBirthDate(
            LocalDate.now().minusYears(10).plusDays(1), shelter
        );
        PetEntity tenYearsPet = petComponent.savePetWithAge(10, shelter);
        PetEntity twelveYearsPet = petComponent.savePetWithAge(12, shelter);

        performGetRequestAndExpectTwoPetsInOrder(
            PET_BASE_URL + "?ageRange=10+ anos",
            twelveYearsPet,
            tenYearsPet
        );
    }

    @Test
    @DisplayName("Should inform error when an unknown filter is used in search")
    void return_inform_error_when_an_unknown_filter_is_used() throws Exception {
        expectBadRequestFrom(
            mockMvc.perform(get(PET_BASE_URL + "?someUnknownProperty=unknownValue"))
        ).andExpectAll(
            jsonPath(ERROR_FIELD_PATH).value("someUnknownProperty"),
            jsonPath(ERROR_MESSAGE_PATH).value(
                "An error occurred while searching for pets due to "
                + "an invalid property reference"
            )
        );
    }

    @Test
    @DisplayName("Should inform error when an unknown ageRange is used in search")
    void return_inform_error_when_an_unknown_age_range_is_used() throws Exception {
        expectBadRequestFrom(
            mockMvc.perform(get(PET_BASE_URL + "?ageRange=unknownValue"))
        ).andExpectAll(
            jsonPath(ERROR_FIELD_PATH).value("someUnknownProperty"),
            jsonPath(ERROR_MESSAGE_PATH).value(
                "An error occurred while searching for pets due to "
                    + "an invalid property reference"
            )
        );
    }

    private void performGetRequestAndExpectTwoPets(final String url,
                                                   final PetEntity firstExpected,
                                                   final PetEntity secondExpected
    ) throws Exception {
        mockMvc.perform(get(url))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").exists())
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES, isA(List.class)))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES, hasSize(2)))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[0].id",
                equalTo(firstExpected.getId().toString())))
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[1].id",
                equalTo(secondExpected.getId().toString())));
    }

    private void performGetRequestAndExpectTwoPetsInOrder(final String url,
                                                          final PetEntity firstExpected,
                                                          final PetEntity secondExpected
    ) throws Exception {
        ResultActions result = mockMvc.perform(get(url))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").exists())
            .andExpect(jsonPath(EMBEDDED_PET_RESPONSES, isA(List.class)));

        result.andExpect(jsonPath(EMBEDDED_PET_RESPONSES + "[*].id",
            containsInRelativeOrder(
                firstExpected.getId().toString(),
                secondExpected.getId().toString()))
        );
    }

    private ResultActions expectBadRequestFrom(final ResultActions result) throws Exception {
        return result.andExpectAll(
            status().isBadRequest(),
            jsonPath(ERROR_HTTP_STATUS_PATH).value(HttpStatus.BAD_REQUEST.name()),
            jsonPath(ERROR_CODE_PATH).value(HttpStatus.BAD_REQUEST.value()),
            jsonPath(ERROR_TIMESTAMP_PATH).isNotEmpty()
        );
    }
}
