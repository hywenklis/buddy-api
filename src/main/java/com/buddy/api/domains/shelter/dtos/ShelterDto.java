package com.buddy.api.domains.shelter.dtos;

import com.buddy.api.domains.pet.dtos.PetDto;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ShelterDto(UUID id,
                         String nameShelter,
                         String nameResponsible,
                         String cpfResponsible,
                         String address,
                         String phoneNumber,
                         String email,
                         String avatar,
                         List<PetDto> pets) {
    public ShelterDto {
        pets = pets == null ? List.of() : List.copyOf(pets);
    }

    @Override
    public List<PetDto> pets() {
        return Collections.unmodifiableList(pets);
    }

    public static class ShelterDtoBuilder {
        public ShelterDtoBuilder pets(final List<PetDto> pets) {
            this.pets = pets == null ? List.of() : List.copyOf(pets);
            return this;
        }
    }
}
