package com.buddy.api.domains.pet.dtos.v2;

import java.util.UUID;
import lombok.Builder;

@Builder
public record PetV2ImageDto(
    UUID id,
    String url,
    Integer displayOrder
) {
}
