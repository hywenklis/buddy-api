package com.buddy.api.components;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PetComponentBuilder {
    public static PetEntity.PetEntityBuilder valid(ShelterEntity shelter) {
        return PetEntity.builder()
            .name(randomAlphabetic(10))
            .specie(randomAlphabetic(10))
            .gender(randomAlphabetic(10))
            .birthDate(LocalDate.now())
            .location(randomAlphabetic(10))
            .weight(Double.valueOf(randomNumeric(2)))
            .description(randomAlphabetic(10))
            .images(List.of())
            .shelter(shelter)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now());
    }

    public static PetEntity.PetEntityBuilder valid(ShelterEntity shelter, Integer age) {
        return PetComponentBuilder.valid(shelter).birthDate(LocalDate.now().minusYears(age));
    }
}
