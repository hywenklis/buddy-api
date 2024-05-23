package com.buddy.api.domains.shelter.services;

import com.buddy.api.domains.shelter.dtos.ShelterDto;

public interface CreateShelter {
    ShelterDto create(ShelterDto shelterDto);
}
