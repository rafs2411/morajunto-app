package br.com.morajunto.morajunto_api.compra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemCompraRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 40, message = "A quantidade deve ter no máximo 40 caracteres")
        String quantidade
) {
}