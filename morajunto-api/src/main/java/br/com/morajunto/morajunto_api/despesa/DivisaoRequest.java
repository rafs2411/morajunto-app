package br.com.morajunto.morajunto_api.despesa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DivisaoRequest(
        @NotNull(message = "Informe o morador da divisão")
        Long moradorId,

        @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Valor inválido")
        BigDecimal valor
) {
}