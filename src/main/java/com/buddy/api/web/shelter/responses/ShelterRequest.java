package com.buddy.api.web.shelter.responses;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record ShelterRequest(@NotBlank(message = "Nome do abrigo obrigatório")
                             String nameShelter,

                             @NotBlank(message = "Responsável do abrigo obrigatório")
                             String nameResponsible,

                             @CPF
                             @NotBlank(message = "CPF do responsável obrigatório")
                             String cpfResponsible,

                             @Email
                             @NotBlank(message = "EMAIL obrigatório")
                             String email,

                             @NotBlank(message = "Senha obrigatória")
                             String password,

                             @NotBlank(message = "Confirmação de senha obrigatória")
                             String passwordConfirm) {
}
