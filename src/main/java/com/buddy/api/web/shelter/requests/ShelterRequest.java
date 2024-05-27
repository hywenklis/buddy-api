package com.buddy.api.web.shelter.requests;

import com.buddy.api.domains.configuration.annotations.Unique;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import static com.buddy.api.domains.enums.UniqueType.CPF;
import static com.buddy.api.domains.enums.UniqueType.EMAIL;

public record ShelterRequest(@NotBlank(message = "Nome do abrigo obrigatório")
                             String nameShelter,

                             @NotBlank(message = "Responsável do abrigo obrigatório")
                             String nameResponsible,

                             @CPF
                             @Unique(value = CPF, message = "CPF must be unique")
                             @NotBlank(message = "CPF do responsável obrigatório")
                             String cpfResponsible,

                             @Email
                             @Unique(value = EMAIL, message = "EMAIL must be unique")
                             @NotBlank(message = "EMAIL obrigatório")
                             String email) {
}
