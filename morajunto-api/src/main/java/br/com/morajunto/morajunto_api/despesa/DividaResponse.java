package br.com.morajunto.morajunto_api.despesa;

import java.math.BigDecimal;

public record DividaResponse(
        Long devedorId,
        String devedorNome,
        String devedorCor,
        Long credorId,
        String credorNome,
        String credorCor,
        BigDecimal valor
) {
}