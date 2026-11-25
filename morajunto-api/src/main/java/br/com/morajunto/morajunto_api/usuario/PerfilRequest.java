package br.com.morajunto.morajunto_api.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "O e-mail pode ter no máximo 160 caracteres")
        String email,

        @Size(max = 500, message = "O link da foto pode ter no máximo 500 caracteres")
        @Pattern(regexp = "^$|^https?://.+", message = "O link da foto precisa começar com http:// ou https://")
        String fotoUrl
) {
}