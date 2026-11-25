package br.com.morajunto.morajunto_api.morador;

public record MoradorResponse(
        Long id,
        String nome,
        String email,
        String cor
) {

    public static MoradorResponse from(Morador morador) {
        return new MoradorResponse(
                morador.getId(),
                morador.getNome(),
                morador.getEmail(),
                morador.getCor()
        );
    }
}