package com.buddy.api.web.shelter.requests;

import static com.buddy.api.commons.enums.UniqueType.CPF;
import static com.buddy.api.commons.enums.UniqueType.EMAIL;

import com.buddy.api.commons.configuration.annotations.Unique;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.br.CPF;

@Builder
public record ShelterRequest(@NotBlank(message = "Shelter name is mandatory")
                             String nameShelter,

                             @NotBlank(message = "Responsible person's name is mandatory")
                             String nameResponsible,

                             @CPF
                             @Unique(value = CPF, message = "CPF must be unique")
                             @NotBlank(message = "Responsible person's CPF is mandatory")
                             String cpfResponsible,

                             @Email
                             @Unique(value = EMAIL, message = "Email must be unique")
                             @NotBlank(message = "Email is mandatory")
                             String email,

                             @NotBlank(message = "Shelter avatar is mandatory")
                             String avatar
) {
}
