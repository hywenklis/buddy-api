package com.buddy.api.web.pets.requests;

import lombok.Builder;

@Builder
public record PetImageRequest(
    String imageUrl
) {
}
