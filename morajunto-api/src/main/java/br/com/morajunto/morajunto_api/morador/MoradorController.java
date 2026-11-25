package br.com.morajunto.morajunto_api.morador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moradores")
@RequiredArgsConstructor
public class MoradorController {

    private final MoradorService moradorService;

    @GetMapping
    public List<MoradorResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return moradorService.listar(usuarioId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MoradorResponse adicionar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MoradorRequest request) {
        return moradorService.adicionar(usuarioId(jwt), request);
    }

    @PutMapping("/{id}")
    public MoradorResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody MoradorRequest request) {
        return moradorService.atualizar(usuarioId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        moradorService.remover(usuarioId(jwt), id);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}