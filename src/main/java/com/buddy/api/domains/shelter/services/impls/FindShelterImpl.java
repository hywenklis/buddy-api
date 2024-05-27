package com.buddy.api.domains.shelter.services.impls;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.mappers.ShelterDomainMapper;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import com.buddy.api.domains.shelter.services.FindShelter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FindShelterImpl implements FindShelter {

    private final ShelterRepository shelterRepository;
    private final ShelterDomainMapper mapper;

    @Override
    public Optional<ShelterDto> findShelterByCpfResponsible(String cpfResponsible) {
        return shelterRepository.findShelterByCpfResponsible(cpfResponsible).map(mapper::mapToDto);
    }

    @Override
    public Optional<ShelterDto> findShelterByEmail(String email) {
        return shelterRepository.findByEmail(email).map(mapper::mapToDto);
    }
}
