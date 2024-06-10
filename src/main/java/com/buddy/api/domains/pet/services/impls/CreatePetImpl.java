package com.buddy.api.domains.pet.services.impls;

import com.buddy.api.domains.pet.dtos.PetDto;
import com.buddy.api.domains.pet.mappers.PetDomainMapper;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.pet.services.CreatePet;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
                .orElseThrow(() -> new EntityNotFoundException("Shelter not found"));

        var petEntity = mapper.mapToEntity(petDto);
        petEntity.setShelter(shelter);

        petEntity.getImages().forEach(image -> image.setPet(petEntity));

        petRepository.save(petEntity);
        shelter.getPets().add(petEntity);
        shelterRepository.save(shelter);
    }
}
