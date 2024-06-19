package com.buddy.api.web.shelter.responses;

import lombok.Builder;

@Builder
public record ShelterResponseCompact(String nameShelter, String avatar) { }
