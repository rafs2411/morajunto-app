package br.com.morajunto.morajunto_api.despesa;

import java.math.BigDecimal;

public record DivisaoResponse(
        Long moradorId,
        String nome,
        String cor,
        BigDecimal valor
) {
}