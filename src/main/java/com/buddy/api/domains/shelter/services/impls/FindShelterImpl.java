package com.buddy.api.domains.shelter.services.impls;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import com.buddy.api.domains.shelter.mappers.ShelterDomainMapper;
import com.buddy.api.domains.shelter.repositories.ShelterRepository;
import com.buddy.api.domains.shelter.services.FindShelter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindShelterImpl implements FindShelter {

    private final ShelterRepository shelterRepository;
    private final ShelterDomainMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ShelterDto> findShelterByCpfResponsible(final String cpfResponsible) {
        return shelterRepository.findShelterByCpfResponsible(cpfResponsible).map(mapper::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShelterDto> findShelterByEmail(final String email) {
        return shelterRepository.findByEmail(email).map(mapper::mapToDto);
    }
}
