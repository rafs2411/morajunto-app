package br.com.morajunto.morajunto_api.entrada;

import java.time.LocalDateTime;

public record SolicitacaoResponse(
        Long id,
        TipoSolicitacao tipo,
        StatusSolicitacao status,
        Long moradiaId,
        String moradiaNome,
        String moradiaEndereco,
        String emailConvidado,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        LocalDateTime expiraEm,
        LocalDateTime criadaEm
) {

    public static SolicitacaoResponse from(SolicitacaoEntrada solicitacao) {
        var moradia = solicitacao.getMoradia();
        var usuario = solicitacao.getUsuario();

        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getTipo(),
                solicitacao.getStatus(),
                moradia.getId(),
                moradia.getNome(),
                moradia.getEndereco(),
                solicitacao.getEmailConvidado(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNome() : null,
                usuario != null ? usuario.getEmail() : null,
                solicitacao.getExpiraEm(),
                solicitacao.getCriadaEm()
        );
    }
}