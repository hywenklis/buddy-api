package com.buddy.api.components;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class PetComponent {

    private static final int DELAY_MICROSECONDS = 1;

    private final PetRepository petRepository;

    public PetComponent(final PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    private PetEntity.PetEntityBuilder valid(final ShelterEntity shelter) {
        return PetEntity.builder()
            .name(randomAlphabetic(10))
            .specie(randomAlphabetic(10))
            .gender(randomAlphabetic(10))
            .birthDate(LocalDate.now())
            .location(randomAlphabetic(10))
            .weight(Double.valueOf(randomNumeric(2)))
            .description(randomAlphabetic(10))
            .images(List.of())
            .shelter(shelter);
    }

    public PetEntity.PetEntityBuilder valid(final ShelterEntity shelter, final Integer age) {
        return valid(shelter).birthDate(LocalDate.now().minusYears(age));
    }

    public PetEntity savePetWithAge(final int age, final ShelterEntity shelter) {
        applyDelay();
        return petRepository.save(valid(shelter, age).build());
    }

    public PetEntity savePetWithName(final String name, final ShelterEntity shelter) {
        applyDelay();
        return petRepository.save(valid(shelter).name(name).build());
    }

    public PetEntity savePetWithBirthDate(final LocalDate birthDate, final ShelterEntity shelter) {
        applyDelay();
        return petRepository.save(valid(shelter).birthDate(birthDate).build());
    }

    private void applyDelay() {
        try {
            TimeUnit.MICROSECONDS.sleep(DELAY_MICROSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
