package br.com.morajunto.morajunto_api.moradia;

public record MoradiaResponse(
        Long id,
        String nome,
        String endereco,
        Long donoId,
        long totalMoradores,
        boolean ativa,
        boolean disponivelParaEntrada,
        Short vagas
) {

    public static MoradiaResponse of(Moradia moradia, long totalMoradores, boolean ativa) {
        return new MoradiaResponse(
                moradia.getId(),
                moradia.getNome(),
                moradia.getEndereco(),
                moradia.getDono().getId(),
                totalMoradores,
                ativa,
                moradia.isDisponivelParaEntrada(),
                moradia.getVagas()
        );
    }
}