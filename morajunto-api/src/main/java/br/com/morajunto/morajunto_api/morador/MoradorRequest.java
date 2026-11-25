package br.com.morajunto.morajunto_api.morador;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MoradorRequest(
        @NotBlank(message = "Informe o nome completo")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres")
        String nome,

        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "O e-mail pode ter no máximo 160 caracteres")
        String email,

        @NotBlank(message = "Informe a cor")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor inválida, use o formato #RRGGBB")
        String cor
) {
}