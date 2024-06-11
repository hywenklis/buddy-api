package com.buddy.api.components;

import static com.buddy.api.builders.pet.PetBuilder.createPetEntity;

import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.entities.PetImageEntity;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PetComponent {

    private final PetRepository petRepository;

    public PetComponent(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public PetEntity createPet(final String name,
                                   final String specie,
                                   final String sex,
                                   final Integer age,
                                   final Double weight,
                                   final String description,
                                   final String avatar,
                                   final List<PetImageEntity> images,
                                   final ShelterEntity shelter
    ) {
        return petRepository.save(createPetEntity(
                name, specie, sex, age, weight, description, avatar, images, shelter
        ));
    }
}
