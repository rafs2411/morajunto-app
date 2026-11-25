package br.com.morajunto.morajunto_api.dashboard;

import br.com.morajunto.morajunto_api.compra.ItemCompraResponse;
import br.com.morajunto.morajunto_api.despesa.ResumoFinanceiroResponse;
import br.com.morajunto.morajunto_api.tarefa.CalendarioItemResponse;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        String mes,
        BigDecimal despesasMes,
        int lancamentosMes,
        int tarefasPendentes,
        int itensParaComprar,
        long moradores,
        ResumoFinanceiroResponse balanco,
        List<CalendarioItemResponse> tarefasHoje,
        List<ItemCompraResponse> previaCompras
) {
}