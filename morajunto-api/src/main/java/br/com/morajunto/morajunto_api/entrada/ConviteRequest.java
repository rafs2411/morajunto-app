package br.com.morajunto.morajunto_api.entrada;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConviteRequest(
        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "O e-mail pode ter no máximo 160 caracteres")
        String email
) {
}