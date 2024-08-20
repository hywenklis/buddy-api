package com.buddy.api.web.shelter.requests;

import static com.buddy.api.commons.enums.UniqueType.CPF;
import static com.buddy.api.commons.enums.UniqueType.EMAIL;

import com.buddy.api.commons.configuration.annotations.Unique;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.br.CPF;

@Builder
public record ShelterRequest(@NotBlank(message = "Name of mandatory shelter")
                             String nameShelter,

                             @NotBlank(message = "Responsible for the mandatory shelter")
                             String nameResponsible,

                             @CPF
                             @Unique(value = CPF, message = "CPF must be unique")
                             @NotBlank(message = "Mandatory responsible person's CPF")
                             String cpfResponsible,

                             @Email
                             @Unique(value = EMAIL, message = "EMAIL must be unique")
                             @NotBlank(message = "Mandatory EMAIL")
                             String email,

                             @NotBlank(message = "Avatar for the mandatory shelter")
                             String avatar) {
}
