package br.com.morajunto.morajunto_api.moradia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MoradiaRequest(
        @NotBlank(message = "Informe o nome da moradia")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "Informe o endereço")
        @Size(max = 255, message = "O endereço pode ter no máximo 255 caracteres")
        String endereco,

        Boolean disponivelParaEntrada,

        @Min(value = 1, message = "Informe pelo menos 1 vaga")
        @Max(value = 50, message = "O máximo é 50 vagas")
        Short vagas
) {
}