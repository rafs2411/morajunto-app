package br.com.morajunto.morajunto_api.auth;

import br.com.morajunto.morajunto_api.usuario.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String fotoUrl
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getFotoUrl()
        );
    }
}