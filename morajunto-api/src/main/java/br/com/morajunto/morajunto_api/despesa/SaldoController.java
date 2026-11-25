package br.com.morajunto.morajunto_api.despesa;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saldos")
@RequiredArgsConstructor
public class SaldoController {

    private final SaldoService saldoService;

    @GetMapping
    public ResumoFinanceiroResponse resumo(@AuthenticationPrincipal Jwt jwt) {
        return saldoService.resumo(Long.valueOf(jwt.getSubject()));
    }
}