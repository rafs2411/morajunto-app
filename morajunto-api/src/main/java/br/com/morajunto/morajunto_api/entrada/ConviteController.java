package br.com.morajunto.morajunto_api.entrada;

import br.com.morajunto.morajunto_api.moradia.MoradiaResponse;
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
@RequestMapping("/api/convites")
@RequiredArgsConstructor
public class ConviteController {

    private final ConviteService conviteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoResponse convidar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConviteRequest request) {
        return conviteService.convidar(usuarioId(jwt), request);
    }

    @GetMapping("/recebidos")
    public List<SolicitacaoResponse> recebidos(@AuthenticationPrincipal Jwt jwt) {
        return conviteService.listarRecebidos(usuarioId(jwt));
    }

    @PostMapping("/{id}/aceitar")
    public MoradiaResponse aceitar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return conviteService.aceitar(usuarioId(jwt), id);
    }

    @PostMapping("/{id}/recusar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recusar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        conviteService.recusar(usuarioId(jwt), id);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}