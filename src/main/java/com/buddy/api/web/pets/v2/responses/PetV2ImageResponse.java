package com.buddy.api.web.pets.v2.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "PetV2ImageResponse", description = "Image associated with a pet")
public record PetV2ImageResponse(
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(example = "https://cdn.buddy.com/images/pets/thor-1.jpg")
    String url,

    @Schema(example = "0")
    Integer displayOrder
) {
}
