package com.buddy.api.web.pets.responses;

import lombok.Builder;

@Builder
public record PetResponse(
        String message
) {}
