package com.buddy.api.domains.pet.services;

import com.buddy.api.domains.pet.dtos.PetDto;

public interface CreatePet {
    void create(PetDto petDto);
}
