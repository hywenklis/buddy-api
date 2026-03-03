package com.buddy.api.domains.shelter.services.impl;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.mappers.ShelterDomainMapper;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import com.buddy.api.domains.shelter.services.CreateShelter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateShelterImpl implements CreateShelter {

    private final ShelterRepository shelterRepository;
    private final ShelterDomainMapper mapper;

    @Override
    @Transactional
    public void create(final ShelterDto shelterDto) {
        log.info("Creating shelter: {}", shelterDto.nameShelter());
        final var savedShelter = shelterRepository.save(mapper.mapToEntity(shelterDto));
        log.info("Shelter created successfully with ID: {}", savedShelter.getId());
    }
}
