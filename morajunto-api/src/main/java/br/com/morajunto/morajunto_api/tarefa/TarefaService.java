package br.com.morajunto.morajunto_api.tarefa;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final TarefaOcorrenciaRepository ocorrenciaRepository;
    private final MoradorRepository moradorRepository;
    private final MoradiaAtivaService moradiaAtivaService;

    @Transactional(readOnly = true)
    public List<TarefaResponse> listar(Long usuarioId) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        return tarefaRepository.findAtivasDaMoradia(moradia.getId()).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public TarefaResponse criar(Long usuarioId, TarefaRequest request) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        validar(request);
        Morador responsavel = buscarResponsavel(request.responsavelId(), moradia.getId());

        Tarefa tarefa = new Tarefa();
        tarefa.setMoradia(moradia);
        aplicar(tarefa, request, responsavel);

        return paraResponse(tarefaRepository.save(tarefa));
    }

    @Transactional
    public TarefaResponse editar(Long usuarioId, Long id, TarefaRequest request) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        Tarefa tarefa = buscarTarefa(id, moradia.getId());
        validar(request);
        Morador responsavel = buscarResponsavel(request.responsavelId(), moradia.getId());

        aplicar(tarefa, request, responsavel);

        return paraResponse(tarefaRepository.save(tarefa));
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        Tarefa tarefa = buscarTarefa(id, moradia.getId());
        tarefa.setAtiva(false);
        tarefaRepository.save(tarefa);
    }

    @Transactional
    public OcorrenciaResponse concluir(Long usuarioId, Long tarefaId, LocalDate data) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        Tarefa tarefa = buscarTarefa(tarefaId, moradia.getId());
        validarDataNaRegra(tarefa, data);

        Morador quem = moradorRepository.findAtivosDoUsuario(usuarioId).stream()
                .filter(m -> m.getMoradia().getId().equals(moradia.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você não faz parte desta moradia"));

        TarefaOcorrencia ocorrencia = ocorrenciaRepository
                .findByTarefaIdAndDataOcorrencia(tarefaId, data)
                .orElseGet(() -> {
                    TarefaOcorrencia nova = new TarefaOcorrencia();
                    nova.setTarefa(tarefa);
                    nova.setDataOcorrencia(data);
                    return nova;
                });

        ocorrencia.setConcluida(true);
        ocorrencia.setConcluidaEm(LocalDateTime.now());
        ocorrencia.setConcluidaPor(quem);

        return paraOcorrenciaResponse(ocorrenciaRepository.save(ocorrencia));
    }

    @Transactional
    public OcorrenciaResponse desmarcar(Long usuarioId, Long tarefaId, LocalDate data) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        Tarefa tarefa = buscarTarefa(tarefaId, moradia.getId());
        validarDataNaRegra(tarefa, data);

        TarefaOcorrencia ocorrencia = ocorrenciaRepository
                .findByTarefaIdAndDataOcorrencia(tarefaId, data)
                .orElse(null);

        if (ocorrencia == null) {
            return new OcorrenciaResponse(tarefaId, data, false, null, null, null);
        }

        ocorrencia.setConcluida(false);
        ocorrencia.setConcluidaEm(null);
        ocorrencia.setConcluidaPor(null);

        return paraOcorrenciaResponse(ocorrenciaRepository.save(ocorrencia));
    }

    @Transactional(readOnly = true)
    public List<CalendarioItemResponse> calendario(Long usuarioId, LocalDate inicio, LocalDate fim,
                                                   StatusTarefa status) {
        if (fim.isBefore(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data final não pode ser anterior à data inicial");
        }
        if (inicio.plusDays(92).isBefore(fim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O período máximo é de 93 dias");
        }

        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        List<Tarefa> tarefas = tarefaRepository.findAtivasNoPeriodo(moradia.getId(), inicio, fim);
        if (tarefas.isEmpty()) {
            return List.of();
        }

        List<Long> ids = tarefas.stream().map(Tarefa::getId).toList();
        Map<String, TarefaOcorrencia> concluidas = new HashMap<>();
        for (TarefaOcorrencia o : ocorrenciaRepository.findDoPeriodo(ids, inicio, fim)) {
            if (o.isConcluida()) {
                concluidas.put(o.getTarefa().getId() + "|" + o.getDataOcorrencia(), o);
            }
        }

        List<CalendarioItemResponse> itens = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
                if (!ocorreEm(tarefa, dia)) {
                    continue;
                }
                TarefaOcorrencia ocorrencia = concluidas.get(tarefa.getId() + "|" + dia);
                boolean concluida = ocorrencia != null;
                if (status == StatusTarefa.PENDENTES && concluida) {
                    continue;
                }
                if (status == StatusTarefa.CONCLUIDAS && !concluida) {
                    continue;
                }
                itens.add(paraCalendarioItem(tarefa, dia, ocorrencia));
            }
        }

        itens.sort(Comparator.comparing(CalendarioItemResponse::data)
                .thenComparing(CalendarioItemResponse::horario, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CalendarioItemResponse::nome));
        return itens;
    }

    private boolean ocorreEm(Tarefa tarefa, LocalDate data) {
        if (data.isBefore(tarefa.getDataInicio())
                || (tarefa.getDataFim() != null && data.isAfter(tarefa.getDataFim()))) {
            return false;
        }
        if (tarefa.getRecorrencia() == Recorrencia.UNICA) {
            return data.equals(tarefa.getDataInicio());
        }
        return tarefa.getDiasSemana().contains(diaSemanaBanco(data));
    }

    private CalendarioItemResponse paraCalendarioItem(Tarefa tarefa, LocalDate data, TarefaOcorrencia ocorrencia) {
        Morador responsavel = tarefa.getResponsavel();
        Morador quem = ocorrencia == null ? null : ocorrencia.getConcluidaPor();
        return new CalendarioItemResponse(
                tarefa.getId(),
                tarefa.getNome(),
                tarefa.getDescricao(),
                data,
                tarefa.getHorario(),
                responsavel.getId(),
                responsavel.getNome(),
                responsavel.getCor(),
                ocorrencia != null,
                quem == null ? null : quem.getId(),
                quem == null ? null : quem.getNome()
        );
    }

    private void validarDataNaRegra(Tarefa tarefa, LocalDate data) {
        if (!ocorreEm(tarefa, data)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esta tarefa não acontece na data informada");
        }
    }

    private int diaSemanaBanco(LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();
        return dia.getValue() % 7;
    }

    private Tarefa buscarTarefa(Long id, Long moradiaId) {
        return tarefaRepository.findAtivaPorId(id, moradiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
    }

    private Morador buscarResponsavel(Long responsavelId, Long moradiaId) {
        return moradorRepository.findByIdAndMoradiaIdAndAtivoTrue(responsavelId, moradiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O responsável precisa ser um morador ativo da moradia"));
    }

    private void validar(TarefaRequest request) {
        if (request.recorrencia() == Recorrencia.SEMANAL
                && (request.diasSemana() == null || request.diasSemana().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe pelo menos um dia da semana para tarefas semanais");
        }
        if (request.dataFim() != null && request.dataFim().isBefore(request.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data final não pode ser anterior à data de início");
        }
    }

    private void aplicar(Tarefa tarefa, TarefaRequest request, Morador responsavel) {
        tarefa.setNome(request.nome().trim());
        tarefa.setDescricao(request.descricao() == null || request.descricao().isBlank()
                ? null
                : request.descricao().trim());
        tarefa.setResponsavel(responsavel);
        tarefa.setRecorrencia(request.recorrencia());
        tarefa.setDataInicio(request.dataInicio());
        tarefa.setHorario(request.horario());

        tarefa.getDiasSemana().clear();
        if (request.recorrencia() == Recorrencia.SEMANAL) {
            tarefa.setDataFim(request.dataFim());
            tarefa.getDiasSemana().addAll(request.diasSemana());
        } else {
            tarefa.setDataFim(request.dataInicio());
        }
    }

    private OcorrenciaResponse paraOcorrenciaResponse(TarefaOcorrencia ocorrencia) {
        Morador quem = ocorrencia.getConcluidaPor();
        return new OcorrenciaResponse(
                ocorrencia.getTarefa().getId(),
                ocorrencia.getDataOcorrencia(),
                ocorrencia.isConcluida(),
                ocorrencia.getConcluidaEm(),
                quem == null ? null : quem.getId(),
                quem == null ? null : quem.getNome()
        );
    }

    private TarefaResponse paraResponse(Tarefa tarefa) {
        Morador responsavel = tarefa.getResponsavel();
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getNome(),
                tarefa.getDescricao(),
                responsavel.getId(),
                responsavel.getNome(),
                responsavel.getCor(),
                tarefa.getRecorrencia(),
                tarefa.getDataInicio(),
                tarefa.getDataFim(),
                tarefa.getHorario(),
                tarefa.getDiasSemana().stream().sorted().toList()
        );
    }
}