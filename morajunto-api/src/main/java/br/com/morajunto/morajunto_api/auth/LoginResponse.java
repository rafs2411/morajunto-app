package br.com.morajunto.morajunto_api.auth;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResponse usuario
) {
}