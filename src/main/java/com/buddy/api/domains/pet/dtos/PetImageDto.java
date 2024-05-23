package com.buddy.api.domains.pet.dtos;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PetImageDto(UUID id,
                          String imageUrl) {
}
