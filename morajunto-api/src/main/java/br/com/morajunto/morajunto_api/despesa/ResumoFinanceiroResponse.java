package br.com.morajunto.morajunto_api.despesa;

import java.util.List;

public record ResumoFinanceiroResponse(
        List<SaldoResponse> saldos,
        List<DividaResponse> dividas
) {
}