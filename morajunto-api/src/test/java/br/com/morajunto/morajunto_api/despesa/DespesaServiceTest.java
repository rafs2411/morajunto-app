package br.com.morajunto.morajunto_api.despesa;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {

    @Mock
    private DespesaRepository despesaRepository;

    @Mock
    private CategoriaDespesaRepository categoriaRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private MoradiaAtivaService moradiaAtivaService;

    @InjectMocks
    private DespesaService despesaService;

    private Moradia moradia;
    private Morador maria;
    private Morador joao;
    private Morador ana;

    @BeforeEach
    void preparar() {
        moradia = new Moradia();
        moradia.setId(1L);

        maria = morador(1L, "Maria Santos", "#FF5A7E");
        joao = morador(3L, "João Oliveira", "#3B82F6");
        ana = morador(5L, "Ana Costa", "#F59E0B");

        CategoriaDespesa categoria = new CategoriaDespesa();
        categoria.setId((short) 1);
        categoria.setNome("Aluguel");

        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);
        when(categoriaRepository.findById((short) 1)).thenReturn(Optional.of(categoria));
        when(moradorRepository.findByIdAndMoradiaIdAndAtivoTrue(1L, 1L)).thenReturn(Optional.of(maria));
    }

    @Test
    @DisplayName("Divisão igual de R$ 100,00 entre 3: o centavo que sobra vai para o menor id")
    void divisaoIgualComCentavoQueSobra() {
        prepararOutrosMoradores();

        despesaService.criar(1L, requisicao(TipoDivisao.IGUAL, "100.00",
                divisao(5L, null), divisao(1L, null), divisao(3L, null)));

        Map<Long, BigDecimal> valores = valoresSalvos();
        assertThat(valores.get(1L)).isEqualByComparingTo("33.34");
        assertThat(valores.get(3L)).isEqualByComparingTo("33.33");
        assertThat(valores.get(5L)).isEqualByComparingTo("33.33");
    }

    @Test
    @DisplayName("Divisão igual de R$ 100,01 entre 3: dois centavos vão para os dois menores ids")
    void divisaoIgualComDoisCentavos() {
        prepararOutrosMoradores();

        despesaService.criar(1L, requisicao(TipoDivisao.IGUAL, "100.01",
                divisao(1L, null), divisao(3L, null), divisao(5L, null)));

        Map<Long, BigDecimal> valores = valoresSalvos();
        assertThat(valores.get(1L)).isEqualByComparingTo("33.34");
        assertThat(valores.get(3L)).isEqualByComparingTo("33.34");
        assertThat(valores.get(5L)).isEqualByComparingTo("33.33");
        assertThat(soma(valores)).isEqualByComparingTo("100.01");
    }

    @Test
    @DisplayName("Sem tipo de divisão informado, a divisão é igual")
    void semTipoDivideIgual() {
        prepararOutrosMoradores();

        despesaService.criar(1L, requisicao(null, "90.00",
                divisao(1L, null), divisao(3L, null), divisao(5L, null)));

        Map<Long, BigDecimal> valores = valoresSalvos();
        assertThat(valores.values()).allSatisfy(valor -> assertThat(valor).isEqualByComparingTo("30.00"));
    }

    @Test
    @DisplayName("Divisão personalizada guarda o valor informado de cada morador")
    void divisaoPersonalizadaGuardaValores() {
        prepararOutrosMoradores();

        despesaService.criar(1L, requisicao(TipoDivisao.PERSONALIZADA, "100.00",
                divisao(1L, "50.00"), divisao(3L, "30.00"), divisao(5L, "20.00")));

        Map<Long, BigDecimal> valores = valoresSalvos();
        assertThat(valores.get(1L)).isEqualByComparingTo("50.00");
        assertThat(valores.get(3L)).isEqualByComparingTo("30.00");
        assertThat(valores.get(5L)).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("Divisão personalizada com soma diferente do total é recusada")
    void divisaoPersonalizadaComSomaErrada() {
        assertThatThrownBy(() -> despesaService.criar(1L, requisicao(TipoDivisao.PERSONALIZADA, "100.00",
                divisao(1L, "50.00"), divisao(3L, "30.00"), divisao(5L, "10.00"))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));

        verify(despesaRepository, never()).save(any(Despesa.class));
    }

    @Test
    @DisplayName("Morador repetido na divisão é recusado")
    void moradorRepetidoNaDivisao() {
        assertThatThrownBy(() -> despesaService.criar(1L, requisicao(TipoDivisao.IGUAL, "100.00",
                divisao(1L, null), divisao(1L, null))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));

        verify(despesaRepository, never()).save(any(Despesa.class));
    }

    @Test
    @DisplayName("Morador que não é desta moradia não entra na divisão")
    void moradorDeOutraMoradia() {
        assertThatThrownBy(() -> despesaService.criar(1L, requisicao(TipoDivisao.IGUAL, "100.00",
                divisao(1L, null), divisao(99L, null))))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(400));

        verify(despesaRepository, never()).save(any(Despesa.class));
    }

    private void prepararOutrosMoradores() {
        when(moradorRepository.findByIdAndMoradiaIdAndAtivoTrue(3L, 1L)).thenReturn(Optional.of(joao));
        when(moradorRepository.findByIdAndMoradiaIdAndAtivoTrue(5L, 1L)).thenReturn(Optional.of(ana));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(chamada -> chamada.getArgument(0));
    }

    private Map<Long, BigDecimal> valoresSalvos() {
        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(despesaRepository).save(salva.capture());
        return salva.getValue().getDivisoes().stream()
                .collect(Collectors.toMap(d -> d.getMorador().getId(), DespesaDivisao::getValorDevido));
    }

    private BigDecimal soma(Map<Long, BigDecimal> valores) {
        return valores.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DespesaRequest requisicao(TipoDivisao tipo, String total, DivisaoRequest... divisoes) {
        return new DespesaRequest(
                "Aluguel",
                (short) 1,
                new BigDecimal(total),
                LocalDate.of(2026, 11, 5),
                null,
                1L,
                tipo,
                List.of(divisoes)
        );
    }

    private DivisaoRequest divisao(Long moradorId, String valor) {
        return new DivisaoRequest(moradorId, valor != null ? new BigDecimal(valor) : null);
    }

    private Morador morador(Long id, String nome, String cor) {
        Morador morador = new Morador();
        morador.setId(id);
        morador.setNome(nome);
        morador.setCor(cor);
        return morador;
    }

    private int status(Throwable erro) {
        return ((ResponseStatusException) erro).getStatusCode().value();
    }
}