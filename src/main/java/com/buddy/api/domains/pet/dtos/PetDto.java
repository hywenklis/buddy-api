package com.buddy.api.domains.pet.dtos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetDto(UUID id,
                     UUID shelterId,
                     String name,
                     String specie,
                     String gender,
                     LocalDate birthDate,
                     String location,
                     Double weight,
                     String description,
                     String avatar,
                     List<PetImageDto> images) {

    public PetDto {
        images = images == null ? List.of() : List.copyOf(images);
    }

    @Override
    public List<PetImageDto> images() {
        return List.copyOf(this.images);
    }
}
