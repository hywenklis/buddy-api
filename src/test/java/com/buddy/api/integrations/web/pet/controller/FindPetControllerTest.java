package com.buddy.api.integrations.web.pet.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buddy.api.components.PetComponentBuilder;
import com.buddy.api.integrations.IntegrationTestAbstract;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GET /v1/pets")
public class FindPetControllerTest extends IntegrationTestAbstract {

    @Test
    @DisplayName("Should return last two pets ordered by createDate descending")
    void return_last_two_pets_ordered_createDate_desc() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        petRepository.save(PetComponentBuilder.valid(shelter).name("Tata").build());
        var lolo = petRepository.save(PetComponentBuilder.valid(shelter).name("Lolo").build());
        var kiki = petRepository.save(PetComponentBuilder.valid(shelter).name("Kiki").build());

        mockMvc.perform(get("/v1/pets?page=0&size=2&sort=createDate,desc"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                jsonPath("$._embedded.petParamsResponseList", isA(List.class)),
                jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[0].id",
                    equalTo(kiki.getId().toString())),
                jsonPath("$._embedded.petParamsResponseList[1].id",
                    equalTo(lolo.getId().toString()))
            );
    }

    @Test
    @DisplayName("Should return pets ordered by createDate descending by default")
    void return_pets_ordered_createDate_desc_default() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var lolo = petRepository.save(PetComponentBuilder.valid(shelter).name("Lolo").build());
        var kiki = petRepository.save(PetComponentBuilder.valid(shelter).name("Kiki").build());

        mockMvc.perform(get("/v1/pets"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                jsonPath("$._embedded.petParamsResponseList", isA(List.class)),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[0].id",
                    equalTo(kiki.getId().toString())),
                jsonPath("$._embedded.petParamsResponseList[1].id",
                    equalTo(lolo.getId().toString()))
            );
    }

    @Test
    @DisplayName("Should return first two pets ordered by createDate ascending")
    void return_first_two_pets_ordered_createDate_asc() throws Exception {
        petImageRepository.deleteAll();
        adoptionRequestRepository.deleteAll();
        petRepository.deleteAll();

        var shelter = shelterComponent.createShelterNoPets();

        var tata = petRepository.save(PetComponentBuilder.valid(shelter).name("Tata").build());
        var lolo = petRepository.save(PetComponentBuilder.valid(shelter).name("Lolo").build());

        petRepository.save(PetComponentBuilder.valid(shelter).name("Kiki").build());

        mockMvc.perform(get("/v1/pets?page=0&size=2&sort=createDate,asc"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                jsonPath("$._embedded.petParamsResponseList", isA(List.class)),
                jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[0].id",
                    equalTo(tata.getId().toString())),
                jsonPath("$._embedded.petParamsResponseList[1].id",
                    equalTo(lolo.getId().toString()))
            );
    }

    @Test
    @DisplayName("Should correctly paginate when only page and size are provided")
    void return_all_pets_when_no_order_defined() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        petRepository.save(PetComponentBuilder.valid(shelter).name("Tata").build());
        petRepository.save(PetComponentBuilder.valid(shelter).name("Lolo").build());
        var kiki = petRepository.save(PetComponentBuilder.valid(shelter).name("Kiki").build());
        var zeze = petRepository.save(PetComponentBuilder.valid(shelter).name("Zeze").build());


        mockMvc.perform(get("/v1/pets?page=0&size=2"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                jsonPath("$._embedded.petParamsResponseList", isA(List.class)),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2))
                jsonPath("$._embedded.petParamsResponseList[0].id",
                    equalTo(zeze.getId().toString())),
                jsonPath("$._embedded.petParamsResponseList[1].id",
                    equalTo(kiki.getId().toString()))
            );
    }

    @Test
    @DisplayName("Should return pet by id")
    void return_pet_by_id() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var pet = petRepository.save(PetComponentBuilder.valid(shelter).build());

        mockMvc.perform(get(String.format("/v1/pets?id=%s", pet.getId())))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                jsonPath("$._embedded.petParamsResponseList", isA(List.class)),
                jsonPath("$._embedded.petParamsResponseList", hasSize(1)),
                jsonPath("$._embedded.petParamsResponseList[0].id",
                    equalTo(pet.getId().toString())),
                jsonPath("$._embedded.petParamsResponseList[0].name",
                    equalTo(pet.getName())),
                jsonPath("$._embedded.petParamsResponseList[0].gender",
                    equalTo(pet.getGender())),
                jsonPath("$._embedded.petParamsResponseList[0].specie",
                    equalTo(pet.getSpecie())),
                jsonPath("$._embedded.petParamsResponseList[0].location",
                    equalTo(pet.getLocation())),
                jsonPath("$._embedded.petParamsResponseList[0].birthDate",
                    equalTo(pet.getBirthDate().toString())),
                jsonPath("$._embedded.petParamsResponseList[0].description",
                    equalTo(pet.getDescription())),
                jsonPath("$._embedded.petParamsResponseList[0].weight",
                    equalTo(pet.getWeight())),
                jsonPath("$._embedded.petParamsResponseList[0].avatar",
                    equalTo(pet.getAvatar())),
                jsonPath("$._embedded.petParamsResponseList[0].images",
                    hasSize(0)),
                jsonPath(
                    "$._embedded.petParamsResponseList[0].shelterResponseCompact"
                        + ".nameShelter", equalTo(shelter.getNameShelter())),
                jsonPath(
                    "$._embedded.petParamsResponseList[0].shelterResponseCompact"
                        + ".avatar", equalTo(shelter.getAvatar()))
            );
    }

    @Test
    @DisplayName("Should return pets with given name")
    void return_pet_by_name() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var firstTata = petRepository.save(PetComponentBuilder.valid(shelter).name("Tata").build());
        petRepository.save(PetComponentBuilder.valid(shelter).name("Lolo").build());
        var secondTata = petRepository.save(PetComponentBuilder.valid(shelter).name("Tata").build());

        mockMvc.perform(get("/v1/pets?name=Tata&page=0&size=2"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                jsonPath("$._embedded.petParamsResponseList", isA(List.class)),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[0].id",
                    equalTo(secondTata.getId().toString())),
                jsonPath("$._embedded.petParamsResponseList[1].id",
                    equalTo(firstTata.getId().toString()))
            );
    }

    @Test
    @DisplayName("Should return pets with age between 0 and 1 years including")
    void return_pet_by_ageRange_0_to_1_years() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var threeMonthsAgo = LocalDate.now().minusMonths(3);

        var zeroYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(threeMonthsAgo)
                .build()
        );

        var almostOneYearAgo = LocalDate.now()
            .minusYears(1)
            .plusDays(1);

        var almostOneYearPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(almostOneYearAgo)
                .build()
        );

        var oneYearAndOneDayAgo = LocalDate.now()
            .minusYears(1)
            .minusDays(1);

        petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(oneYearAndOneDayAgo)
                .build()
        );

        assertThat(almostOneYearPet.getCreateDate()).isAfter(zeroYearsPet.getCreateDate());

        mockMvc.perform(get("/v1/pets?ageRange=0-1 anos"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                jsonPath("$._embedded.petParamsResponseList", isA(List.class)),
                jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[*].id",
                    containsInRelativeOrder(
                        almostOneYearPet.getId().toString(),
                        zeroYearsPet.getId().toString()
                    ))
            );
    }

    @Test
    @DisplayName("Should return pets with 1 <= age < 2")
    void return_pet_by_ageRange_1_to_2_years() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var threeMonthsAgo = LocalDate.now().minusMonths(3);

        petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(threeMonthsAgo)
                .build()
        );

        var oneYearPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter, 1)
                .build()
        );

        var almostTwoYearsAgo = LocalDate.now()
            .minusYears(2)
            .plusDays(1);

        var almostTwoYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(almostTwoYearsAgo)
                .build()
        );

        petRepository.save(
            PetComponentBuilder
                .valid(shelter, 2)
                .build()
        );

        mockMvc.perform(get("/v1/pets?ageRange=1-2 anos"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[*].id",
                    containsInRelativeOrder(
                        almostTwoYearsPet.getId().toString(),
                        oneYearPet.getId().toString()
                    ))
            );
    }

    @Test
    @DisplayName("Should return pets with 2 <= age < 3")
    void return_pet_by_ageRange_2_to_3_years() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var almostTwoYearsAgo = LocalDate.now()
            .minusYears(2)
            .plusDays(1);

        petRepository.save(
            PetComponentBuilder
                .valid(shelter, 1)
                .birthDate(almostTwoYearsAgo)
                .build()
        );

        var twoYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter, 2)
                .build()
        );

        var almostThreeYearsAgo = LocalDate.now()
            .minusYears(3)
            .plusDays(1);

        var almostThreeYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(almostThreeYearsAgo)
                .build()
        );

        petRepository.save(
            PetComponentBuilder
                .valid(shelter, 3)
                .build()
        );

        mockMvc.perform(get("/v1/pets?ageRange=2-3 anos"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[*].id",
                    containsInRelativeOrder(
                        almostThreeYearsPet.getId().toString(),
                        twoYearsPet.getId().toString()
                    ))
            );
    }

    @Test
    @DisplayName("Should return pets with 3 <= age < 5")
    void return_pet_by_ageRange_3_to_5_years() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var almostThreeYearsAgo = LocalDate.now()
            .minusYears(3)
            .plusDays(1);

        petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(almostThreeYearsAgo)
                .build()
        );

        var threeYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter, 3)
                .build()
        );

        var oneDayLessThanFiveYearsAgo = LocalDate.now()
            .minusYears(5)
            .plusDays(1);

        var almostFiveYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(oneDayLessThanFiveYearsAgo)
                .build()
        );

        petRepository.save(
            PetComponentBuilder
                .valid(shelter, 5)
                .build()
        );

        mockMvc.perform(get("/v1/pets?ageRange=3-5 anos"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[*].id",
                    containsInRelativeOrder(
                        almostFiveYearsPet.getId().toString(),
                        threeYearsPet.getId().toString()
                    ))
            );
    }

    @Test
    @DisplayName("Should return pets with 5 <= age 10")
    void return_pet_by_ageRange_5_to_10_years() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var almostFiveYearsAgo = LocalDate.now()
            .minusYears(5)
            .plusDays(1);

        petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(almostFiveYearsAgo)
                .build()
        );

        var fiveYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter, 5)
                .build()
        );

        var almostTenYearsAgo = LocalDate.now()
            .minusYears(10)
            .plusDays(1);

        var almostTenYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(almostTenYearsAgo)
            .build()
        );

        petRepository.save(
            PetComponentBuilder
                .valid(shelter, 10)
                .build()
        );

        mockMvc.perform(get("/v1/pets?ageRange=5-10 anos"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[*].id",
                    containsInRelativeOrder(
                        almostTenYearsPet.getId().toString(),
                        fiveYearsPet.getId().toString()
                    ))
            );
    }

    @Test
    @DisplayName("Should return pets with 10 <= age")
    void return_pet_by_ageRange_10_plus_years() throws Exception {
        var shelter = shelterComponent.createShelterNoPets();

        var almostTenYearsAgo = LocalDate.now()
            .minusYears(10)
            .plusDays(1);

        petRepository.save(
            PetComponentBuilder
                .valid(shelter)
                .birthDate(almostTenYearsAgo)
                .build()
        );

        var tenYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter, 10)
                .build()
        );

        var twelveYearsPet = petRepository.save(
            PetComponentBuilder
                .valid(shelter, 12)
                .build()
        );

        mockMvc.perform(get("/v1/pets?ageRange=10+ anos"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$._embedded").exists(),
                //jsonPath("$._embedded.petParamsResponseList", hasSize(2)),
                jsonPath("$._embedded.petParamsResponseList[*].id",
                    containsInRelativeOrder(
                        twelveYearsPet.getId().toString(),
                        tenYearsPet.getId().toString()
                    ))
            );
    }
}
