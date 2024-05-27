package com.buddy.api.domains.shelter.services;

import com.buddy.api.domains.shelter.dtos.ShelterDto;
import java.util.Optional;

public interface FindShelter {
    Optional<ShelterDto> findShelterByCpfResponsible(String cpfResponsible);

    Optional<ShelterDto> findShelterByEmail(String email);
}
