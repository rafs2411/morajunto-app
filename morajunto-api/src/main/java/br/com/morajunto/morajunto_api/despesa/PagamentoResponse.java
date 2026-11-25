package br.com.morajunto.morajunto_api.despesa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoResponse(
        Long id,
        Long pagadorId,
        String pagadorNome,
        String pagadorCor,
        Long recebedorId,
        String recebedorNome,
        String recebedorCor,
        BigDecimal valor,
        LocalDate dataPagamento,
        String observacao
) {
}