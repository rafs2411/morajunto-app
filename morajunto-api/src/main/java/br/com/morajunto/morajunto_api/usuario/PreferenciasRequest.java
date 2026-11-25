package br.com.morajunto.morajunto_api.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PreferenciasRequest(
        @NotNull(message = "Informe o tema")
        Tema tema,

        @NotBlank(message = "Informe o idioma")
        @Pattern(regexp = "pt-BR|en-US|es-ES", message = "O idioma precisa ser pt-BR, en-US ou es-ES")
        String idioma,

        @NotNull(message = "Informe se quer notificações de tarefas")
        Boolean notificarTarefas,

        @NotNull(message = "Informe se quer notificações de despesas")
        Boolean notificarDespesas
) {
}