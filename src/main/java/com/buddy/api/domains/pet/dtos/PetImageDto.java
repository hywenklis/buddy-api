package com.buddy.api.domains.pet.dtos;

import java.util.UUID;
import lombok.Builder;

@Builder
public record PetImageDto(UUID id, String imageUrl) { }
