package com.buddy.api.domains.shelter.services.impls;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.mappers.ShelterDomainMapper;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import com.buddy.api.domains.shelter.services.CreateShelter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateShelterImpl implements CreateShelter {

    private final ShelterRepository shelterRepository;
    private final ShelterDomainMapper mapper;

    @Override
    @Transactional
    public void create(final ShelterDto shelterDto) {
        final var savedShelter = shelterRepository.save(mapper.mapToEntity(shelterDto));
        mapper.mapToDto(savedShelter);
    }
}
