package br.com.morajunto.morajunto_api.moradia;

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
@RequestMapping("/api/moradias")
@RequiredArgsConstructor
public class MoradiaController {

    private final MoradiaService moradiaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MoradiaResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MoradiaRequest request) {
        return moradiaService.criar(usuarioId(jwt), request);
    }

    @GetMapping
    public List<MoradiaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return moradiaService.listar(usuarioId(jwt));
    }

    @PostMapping("/{id}/selecionar")
    public MoradiaResponse selecionar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return moradiaService.selecionar(usuarioId(jwt), id);
    }

    @PutMapping("/{id}")
    public MoradiaResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                     @Valid @RequestBody MoradiaRequest request) {
        return moradiaService.atualizar(usuarioId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        moradiaService.excluir(usuarioId(jwt), id);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}