package com.buddy.api.web.pets.responses;

import com.buddy.api.web.shelter.responses.ShelterResponseCompact;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PetParamsResponse(UUID id,
                                UUID shelterId,
                                String name,
                                String avatar,
                                String specie,
                                String gender,
                                LocalDate birthDate,
                                String location,
                                Double weight,
                                String description,
                                ShelterResponseCompact shelterResponseCompact,
                                List<PetImageResponse> images) {
}
