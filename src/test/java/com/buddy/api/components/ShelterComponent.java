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

    public ShelterComponent(final ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    public void createShelter(final String nameShelter,
                              final String nameResponsible,
                              final String cpfResponsible,
                              final String email,
                              final String address,
                              final String phoneNumber,
                              final String avatar,
                              final List<PetEntity> pets
    ) {
        shelterRepository.save(createShelterEntity(
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

    public ShelterEntity createShelterNoPets() {
        return shelterRepository.save(ShelterBuilder.createShelterEntityNoPets());
    }
}
