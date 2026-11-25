package br.com.morajunto.morajunto_api.usuario;

import br.com.morajunto.morajunto_api.auth.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.buscar(usuarioId(jwt));
    }

    @PutMapping("/me")
    public UsuarioResponse atualizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest request) {
        return usuarioService.atualizarPerfil(usuarioId(jwt), request);
    }

    @PutMapping("/me/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trocarSenha(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TrocaSenhaRequest request) {
        usuarioService.trocarSenha(usuarioId(jwt), request);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}