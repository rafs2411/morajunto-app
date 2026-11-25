package br.com.morajunto.morajunto_api.entrada;

public record MoradiaDisponivelResponse(
        Long id,
        String nome,
        String endereco,
        long totalMoradores,
        Short vagas,
        boolean pedidoPendente
) {
}