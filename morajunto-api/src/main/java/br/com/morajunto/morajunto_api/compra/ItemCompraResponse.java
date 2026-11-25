package br.com.morajunto.morajunto_api.compra;

import java.time.LocalDateTime;

public record ItemCompraResponse(
        Long id,
        String nome,
        String quantidade,
        boolean comprado,
        LocalDateTime compradoEm,
        Long compradoPorId,
        String compradoPorNome,
        Long criadoPorId,
        String criadoPorNome,
        LocalDateTime criadoEm
) {
}