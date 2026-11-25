package br.com.morajunto.morajunto_api.entrada;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping("/moradias-disponiveis")
    public List<MoradiaDisponivelResponse> disponiveis(@AuthenticationPrincipal Jwt jwt) {
        return pedidoService.listarDisponiveis(usuarioId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoResponse pedir(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PedidoRequest request) {
        return pedidoService.pedir(usuarioId(jwt), request);
    }

    @GetMapping("/enviados")
    public List<SolicitacaoResponse> enviados(@AuthenticationPrincipal Jwt jwt) {
        return pedidoService.listarEnviados(usuarioId(jwt));
    }

    @GetMapping("/recebidos")
    public List<SolicitacaoResponse> recebidos(@AuthenticationPrincipal Jwt jwt) {
        return pedidoService.listarRecebidos(usuarioId(jwt));
    }

    @PostMapping("/{id}/aceitar")
    public SolicitacaoResponse aceitar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return pedidoService.aceitar(usuarioId(jwt), id);
    }

    @PostMapping("/{id}/recusar")
    public SolicitacaoResponse recusar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return pedidoService.recusar(usuarioId(jwt), id);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}