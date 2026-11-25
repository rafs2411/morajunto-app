package br.com.morajunto.morajunto_api.despesa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoRequest(
        @NotNull(message = "Informe quem pagou")
        Long pagadorId,

        @NotNull(message = "Informe quem recebeu")
        Long recebedorId,

        @NotNull(message = "Informe o valor")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Valor inválido")
        BigDecimal valor,

        LocalDate dataPagamento,

        @Size(max = 255, message = "A observação pode ter no máximo 255 caracteres")
        String observacao
) {
}