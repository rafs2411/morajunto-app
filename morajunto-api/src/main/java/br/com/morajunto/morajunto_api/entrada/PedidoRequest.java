package br.com.morajunto.morajunto_api.entrada;

import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
        @NotNull(message = "Informe a moradia")
        Long moradiaId
) {
}