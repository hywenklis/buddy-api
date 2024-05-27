package com.buddy.api.domains.shelter.dtos;

import com.buddy.api.domains.pet.dtos.PetDto;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ShelterDto(UUID id,
                         String nameShelter,
                         String nameResponsible,
                         String cpfResponsible,
                         String address,
                         String phoneNumber,
                         String email,
                         List<PetDto> pets) {
}
