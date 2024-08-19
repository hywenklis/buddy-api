package com.buddy.api.domains.pet.services.impls;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.mappers.PetDomainMapper;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.pet.services.CreatePet;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatePetImpl implements CreatePet {

    private final PetRepository petRepository;
    private final PetDomainMapper mapper;
    private final ShelterRepository shelterRepository;

    @Override
    @Transactional
    public void create(final PetDto petDto) {
        var shelter = shelterRepository.findById(petDto.shelterId())
            .orElseThrow(() -> new NotFoundException("shelterId", "Shelter not found"));

        var petEntity = mapper.mapToEntity(petDto);
        petEntity.setShelter(shelter);

        if (petEntity.getImages() != null) {
            petEntity.getImages().forEach(image -> image.setPet(petEntity));
        }

        petRepository.save(petEntity);

        if (shelter.getPets() == null) {
            shelter.setPets(new ArrayList<>());
        }

        shelter.getPets().add(petEntity);
        shelterRepository.save(shelter);
    }
}
