package br.com.morajunto.morajunto_api.despesa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @GetMapping
    public List<PagamentoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return pagamentoService.listar(usuarioId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoResponse registrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PagamentoRequest request) {
        return pagamentoService.registrar(usuarioId(jwt), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        pagamentoService.excluir(usuarioId(jwt), id);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}