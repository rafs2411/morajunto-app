package br.com.morajunto.morajunto_api.tarefa;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.time.YearMonth;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService tarefaService;

    @GetMapping
    public List<TarefaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return tarefaService.listar(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/calendario")
    public List<CalendarioItemResponse> calendario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "TODAS") StatusTarefa status) {
        return tarefaService.calendario(Long.valueOf(jwt.getSubject()), inicio, fim, status);
    }

    @GetMapping("/calendario/mes")
    public List<CalendarioItemResponse> calendarioDoMes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @RequestParam(defaultValue = "TODAS") StatusTarefa status) {
        return tarefaService.calendario(Long.valueOf(jwt.getSubject()), mes.atDay(1), mes.atEndOfMonth(), status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TarefaRequest request) {
        return tarefaService.criar(Long.valueOf(jwt.getSubject()), request);
    }

    @PutMapping("/{id}")
    public TarefaResponse editar(@AuthenticationPrincipal Jwt jwt,
                                 @PathVariable Long id,
                                 @Valid @RequestBody TarefaRequest request) {
        return tarefaService.editar(Long.valueOf(jwt.getSubject()), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        tarefaService.excluir(Long.valueOf(jwt.getSubject()), id);
    }

    @PostMapping("/{id}/concluir")
    public OcorrenciaResponse concluir(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable Long id,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return tarefaService.concluir(Long.valueOf(jwt.getSubject()), id, data);
    }

    @DeleteMapping("/{id}/concluir")
    public OcorrenciaResponse desmarcar(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable Long id,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return tarefaService.desmarcar(Long.valueOf(jwt.getSubject()), id, data);
    }
}