package br.com.morajunto.morajunto_api.despesa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/despesas")
@RequiredArgsConstructor
public class DespesaController {

    private final DespesaService despesaService;

    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() {
        return despesaService.listarCategorias();
    }

    @GetMapping
    public List<DespesaResponse> listar(@AuthenticationPrincipal Jwt jwt,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return despesaService.listar(usuarioId(jwt), mes);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DespesaResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DespesaRequest request) {
        return despesaService.criar(usuarioId(jwt), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        despesaService.excluir(usuarioId(jwt), id);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}