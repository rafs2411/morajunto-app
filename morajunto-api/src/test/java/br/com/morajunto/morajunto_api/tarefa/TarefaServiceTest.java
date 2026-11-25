package br.com.morajunto.morajunto_api.tarefa;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 11, 16);
    private static final LocalDate TERCA = LocalDate.of(2026, 11, 17);
    private static final LocalDate QUINTA = LocalDate.of(2026, 11, 19);
    private static final LocalDate SEXTA = LocalDate.of(2026, 11, 20);
    private static final LocalDate DOMINGO = LocalDate.of(2026, 11, 22);

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private TarefaOcorrenciaRepository ocorrenciaRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private MoradiaAtivaService moradiaAtivaService;

    @InjectMocks
    private TarefaService tarefaService;

    private Moradia moradia;
    private Morador maria;

    @BeforeEach
    void preparar() {
        moradia = new Moradia();
        moradia.setId(1L);

        maria = new Morador();
        maria.setId(1L);
        maria.setNome("Maria Santos");
        maria.setCor("#FF5A7E");
        maria.setMoradia(moradia);
    }

    @Test
    @DisplayName("Tarefa semanal sem dias da semana é recusada")
    void semanalSemDias() {
        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);

        assertThatThrownBy(() -> tarefaService.criar(1L,
                requisicao(Recorrencia.SEMANAL, SEGUNDA, null, Set.of())))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));

        verify(tarefaRepository, never()).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("Data final antes da data de início é recusada")
    void dataFimAntesDoInicio() {
        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);

        assertThatThrownBy(() -> tarefaService.criar(1L,
                requisicao(Recorrencia.SEMANAL, QUINTA, SEGUNDA, Set.of(1))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));
    }

    @Test
    @DisplayName("Tarefa única ignora os dias da semana e termina no mesmo dia em que começa")
    void tarefaUnicaIgnoraDias() {
        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);
        when(moradorRepository.findByIdAndMoradiaIdAndAtivoTrue(1L, 1L)).thenReturn(Optional.of(maria));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(chamada -> chamada.getArgument(0));

        tarefaService.criar(1L, requisicao(Recorrencia.UNICA, SEXTA, LocalDate.of(2026, 12, 31), Set.of(1, 4)));

        ArgumentCaptor<Tarefa> salva = ArgumentCaptor.forClass(Tarefa.class);
        verify(tarefaRepository).save(salva.capture());
        assertThat(salva.getValue().getDataFim()).isEqualTo(SEXTA);
        assertThat(salva.getValue().getDiasSemana()).isEmpty();
    }

    @Test
    @DisplayName("Responsável que não mora na moradia é recusado")
    void responsavelDeOutraMoradia() {
        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);

        assertThatThrownBy(() -> tarefaService.criar(1L,
                requisicao(Recorrencia.SEMANAL, SEGUNDA, null, Set.of(1))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));

        verify(tarefaRepository, never()).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("Concluir numa terça uma tarefa de segunda e quinta é recusado")
    void concluirEmDiaForaDaRegra() {
        prepararTarefa(semanal());

        assertThatThrownBy(() -> tarefaService.concluir(1L, 10L, TERCA))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));

        verify(ocorrenciaRepository, never()).save(any(TarefaOcorrencia.class));
    }

    @Test
    @DisplayName("Concluir antes da data de início da tarefa é recusado")
    void concluirAntesDoInicio() {
        prepararTarefa(semanal());

        assertThatThrownBy(() -> tarefaService.concluir(1L, 10L, LocalDate.of(2026, 11, 9)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));
    }

    @Test
    @DisplayName("Tarefa única só pode ser concluída no seu dia")
    void tarefaUnicaEmOutroDia() {
        Tarefa unica = semanal();
        unica.setRecorrencia(Recorrencia.UNICA);
        unica.setDataInicio(SEXTA);
        unica.setDataFim(SEXTA);
        unica.getDiasSemana().clear();
        prepararTarefa(unica);

        assertThatThrownBy(() -> tarefaService.concluir(1L, 10L, LocalDate.of(2026, 11, 21)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));
    }

    @Test
    @DisplayName("Concluir numa segunda registra quem concluiu")
    void concluirEmDiaValido() {
        prepararTarefa(semanal());
        when(moradorRepository.findAtivosDoUsuario(1L)).thenReturn(List.of(maria));
        when(ocorrenciaRepository.save(any(TarefaOcorrencia.class))).thenAnswer(chamada -> chamada.getArgument(0));

        tarefaService.concluir(1L, 10L, SEGUNDA);

        ArgumentCaptor<TarefaOcorrencia> salva = ArgumentCaptor.forClass(TarefaOcorrencia.class);
        verify(ocorrenciaRepository).save(salva.capture());
        assertThat(salva.getValue().isConcluida()).isTrue();
        assertThat(salva.getValue().getDataOcorrencia()).isEqualTo(SEGUNDA);
        assertThat(salva.getValue().getConcluidaPor()).isSameAs(maria);
        assertThat(salva.getValue().getConcluidaEm()).isNotNull();
    }

    @Test
    @DisplayName("Calendário com mais de 93 dias é recusado")
    void calendarioLongoDemais() {
        assertThatThrownBy(() -> tarefaService.calendario(1L, SEGUNDA, SEGUNDA.plusDays(100), StatusTarefa.TODAS))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));
    }

    @Test
    @DisplayName("Calendário da semana mostra a tarefa na segunda e na quinta")
    void calendarioMostraOsDiasCertos() {
        prepararCalendario();

        List<CalendarioItemResponse> itens = tarefaService.calendario(1L, SEGUNDA, DOMINGO, StatusTarefa.TODAS);

        assertThat(itens).extracting(CalendarioItemResponse::data).containsExactly(SEGUNDA, QUINTA);
    }

    @Test
    @DisplayName("Calendário de pendentes esconde o dia já concluído")
    void calendarioPendentesEscondeConcluida() {
        prepararCalendario();

        List<CalendarioItemResponse> itens = tarefaService.calendario(1L, SEGUNDA, DOMINGO, StatusTarefa.PENDENTES);

        assertThat(itens).extracting(CalendarioItemResponse::data).containsExactly(QUINTA);
    }

    private void prepararTarefa(Tarefa tarefa) {
        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);
        when(tarefaRepository.findAtivaPorId(10L, 1L)).thenReturn(Optional.of(tarefa));
    }

    private void prepararCalendario() {
        Tarefa tarefa = semanal();

        TarefaOcorrencia feita = new TarefaOcorrencia();
        feita.setTarefa(tarefa);
        feita.setDataOcorrencia(SEGUNDA);
        feita.setConcluida(true);
        feita.setConcluidaPor(maria);

        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);
        when(tarefaRepository.findAtivasNoPeriodo(1L, SEGUNDA, DOMINGO)).thenReturn(List.of(tarefa));
        when(ocorrenciaRepository.findDoPeriodo(any(), any(), any())).thenReturn(List.of(feita));
    }

    private Tarefa semanal() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(10L);
        tarefa.setMoradia(moradia);
        tarefa.setNome("Lavar a louça");
        tarefa.setResponsavel(maria);
        tarefa.setRecorrencia(Recorrencia.SEMANAL);
        tarefa.setDataInicio(LocalDate.of(2026, 11, 10));
        tarefa.getDiasSemana().addAll(Set.of(1, 4));
        tarefa.setAtiva(true);
        return tarefa;
    }

    private TarefaRequest requisicao(Recorrencia recorrencia, LocalDate inicio, LocalDate fim, Set<Integer> dias) {
        return new TarefaRequest("Lavar a louça", null, 1L, recorrencia, inicio, fim, null, dias);
    }

    private int status(Throwable erro) {
        return ((ResponseStatusException) erro).getStatusCode().value();
    }
}