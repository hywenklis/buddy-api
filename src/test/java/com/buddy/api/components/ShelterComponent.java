package com.buddy.api.components;

import static com.buddy.api.builders.shelter.ShelterBuilder.createShelterEntity;

import com.buddy.api.builders.shelter.ShelterBuilder;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ShelterComponent {

    private final ShelterRepository shelterRepository;

    public ShelterComponent(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    public ShelterEntity createShelter(final String nameShelter,
                                       final String nameResponsible,
                                       final String cpfResponsible,
                                       final String email,
                                       final String address,
                                       final String phoneNumber,
                                       final String avatar,
                                       final List<PetEntity> pets
    ) {
        return shelterRepository.save(createShelterEntity(
                        nameShelter,
                        nameResponsible,
                        cpfResponsible,
                        email,
                        address,
                        phoneNumber,
                        avatar,
                        pets
                )
        );
    }

    public ShelterEntity createShelter() {
        return shelterRepository.save(ShelterBuilder.createShelterEntity());
    }

    public ShelterEntity createShelterNoPets() {
        return shelterRepository.save(ShelterBuilder.createShelterEntityNoPets());
    }
}
