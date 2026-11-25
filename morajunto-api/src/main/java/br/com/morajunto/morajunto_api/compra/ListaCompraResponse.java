package br.com.morajunto.morajunto_api.compra;

import java.util.List;

public record ListaCompraResponse(
        List<ItemCompraResponse> paraComprar,
        List<ItemCompraResponse> comprados
) {
}