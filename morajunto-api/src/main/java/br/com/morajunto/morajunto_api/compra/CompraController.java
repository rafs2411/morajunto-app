package br.com.morajunto.morajunto_api.compra;

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

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    public ListaCompraResponse listar(@AuthenticationPrincipal Jwt jwt) {
        return compraService.listar(Long.valueOf(jwt.getSubject()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemCompraResponse criar(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody ItemCompraRequest request) {
        return compraService.criar(Long.valueOf(jwt.getSubject()), request);
    }

    @PutMapping("/{id}")
    public ItemCompraResponse editar(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable Long id,
                                     @Valid @RequestBody ItemCompraRequest request) {
        return compraService.editar(Long.valueOf(jwt.getSubject()), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        compraService.excluir(Long.valueOf(jwt.getSubject()), id);
    }

    @PostMapping("/{id}/comprado")
    public ItemCompraResponse marcarComprado(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return compraService.marcarComprado(Long.valueOf(jwt.getSubject()), id);
    }

    @DeleteMapping("/{id}/comprado")
    public ItemCompraResponse desmarcarComprado(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return compraService.desmarcarComprado(Long.valueOf(jwt.getSubject()), id);
    }
}