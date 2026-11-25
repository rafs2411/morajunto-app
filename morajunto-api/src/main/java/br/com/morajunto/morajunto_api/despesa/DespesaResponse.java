package br.com.morajunto.morajunto_api.despesa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DespesaResponse(
        Long id,
        String nome,
        Short categoriaId,
        String categoria,
        BigDecimal valorTotal,
        LocalDate dataDespesa,
        String descricao,
        Long pagoPorId,
        String pagoPorNome,
        String pagoPorCor,
        TipoDivisao tipoDivisao,
        List<DivisaoResponse> divisoes
) {
}