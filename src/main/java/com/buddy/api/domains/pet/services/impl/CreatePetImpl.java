package com.buddy.api.domains.pet.services.impl;

import com.buddy.api.commons.exceptions.NotFoundException;
import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.mappers.PetDomainMapper;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.pet.services.CreatePet;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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

        log.info("Creating pet '{}' for shelter ID: {}", petDto.name(), petDto.shelterId());
        petRepository.save(petEntity);
        log.info("Pet created successfully with ID: {}", petEntity.getId());
    }
}
