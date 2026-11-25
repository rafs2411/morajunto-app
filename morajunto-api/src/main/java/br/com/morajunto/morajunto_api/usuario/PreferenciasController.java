package br.com.morajunto.morajunto_api.usuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/me/preferencias")
@RequiredArgsConstructor
public class PreferenciasController {

    private final PreferenciasService preferenciasService;

    @GetMapping
    public PreferenciasResponse buscar(@AuthenticationPrincipal Jwt jwt) {
        return preferenciasService.buscar(Long.valueOf(jwt.getSubject()));
    }

    @PutMapping
    public PreferenciasResponse atualizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PreferenciasRequest request) {
        return preferenciasService.atualizar(Long.valueOf(jwt.getSubject()), request);
    }
}