package br.com.morajunto.morajunto_api.despesa;

import java.math.BigDecimal;

public record SaldoResponse(
        Long moradorId,
        String nome,
        String cor,
        BigDecimal saldo
) {
}