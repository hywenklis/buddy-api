package com.buddy.api.domains.pet.dtos;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PetDto(UUID id,
                     String name,
                     String specie,
                     String sex,
                     Integer age,
                     Double weight,
                     String description,
                     String avatar,
                     List<PetImageDto> images) {
}
