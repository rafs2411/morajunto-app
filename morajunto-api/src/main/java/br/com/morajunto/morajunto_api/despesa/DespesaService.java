package br.com.morajunto.morajunto_api.despesa;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DespesaService {

    private static final BigDecimal UM_CENTAVO = new BigDecimal("0.01");

    private final DespesaRepository despesaRepository;
    private final CategoriaDespesaRepository categoriaRepository;
    private final MoradorRepository moradorRepository;
    private final MoradiaAtivaService moradiaAtivaService;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(categoria -> new CategoriaResponse(categoria.getId(), categoria.getNome()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DespesaResponse> listar(Long usuarioId, YearMonth mes) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        YearMonth referencia = mes != null ? mes : YearMonth.now();

        return despesaRepository.findDoPeriodo(moradia.getId(), referencia.atDay(1), referencia.atEndOfMonth())
                .stream()
                .map(this::montar)
                .toList();
    }

    @Transactional
    public DespesaResponse criar(Long usuarioId, DespesaRequest request) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);

        CategoriaDespesa categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, "Categoria inválida"));

        Morador pagoPor = buscarMorador(moradia, request.pagoPorMoradorId());
        TipoDivisao tipo = request.tipoDivisao() != null ? request.tipoDivisao() : TipoDivisao.IGUAL;
        BigDecimal total = request.valorTotal().setScale(2, RoundingMode.UNNECESSARY);

        Despesa despesa = new Despesa();
        despesa.setMoradia(moradia);
        despesa.setCategoria(categoria);
        despesa.setPagoPor(pagoPor);
        despesa.setNome(request.nome().trim());
        despesa.setDescricao(textoOpcional(request.descricao()));
        despesa.setValorTotal(total);
        despesa.setDataDespesa(request.dataDespesa());
        despesa.setTipoDivisao(tipo);
        despesa.getDivisoes().addAll(montarDivisoes(despesa, moradia, request.divisoes(), tipo, total));

        return montar(despesaRepository.save(despesa));
    }

    @Transactional
    public void excluir(Long usuarioId, Long despesaId) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        Despesa despesa = despesaRepository.findByIdAndMoradiaId(despesaId, moradia.getId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Despesa não encontrada"));
        despesaRepository.delete(despesa);
    }

    private List<DespesaDivisao> montarDivisoes(Despesa despesa, Moradia moradia, List<DivisaoRequest> pedidos,
                                                TipoDivisao tipo, BigDecimal total) {
        Set<Long> vistos = new HashSet<>();
        for (DivisaoRequest pedido : pedidos) {
            if (!vistos.add(pedido.moradorId())) {
                throw erro(HttpStatus.BAD_REQUEST, "Morador repetido na divisão");
            }
        }

        List<DivisaoRequest> ordenados = pedidos.stream()
                .sorted(Comparator.comparing(DivisaoRequest::moradorId))
                .toList();

        List<BigDecimal> valores = tipo == TipoDivisao.IGUAL
                ? dividirIgual(total, ordenados.size())
                : valoresPersonalizados(ordenados, total);

        List<DespesaDivisao> divisoes = new ArrayList<>();
        for (int i = 0; i < ordenados.size(); i++) {
            DespesaDivisao divisao = new DespesaDivisao();
            divisao.setDespesa(despesa);
            divisao.setMorador(buscarMorador(moradia, ordenados.get(i).moradorId()));
            divisao.setValorDevido(valores.get(i));
            divisoes.add(divisao);
        }
        return divisoes;
    }

    private List<BigDecimal> dividirIgual(BigDecimal total, int quantidade) {
        BigDecimal base = total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.DOWN);
        int centavosRestantes = total.subtract(base.multiply(BigDecimal.valueOf(quantidade)))
                .movePointRight(2)
                .intValueExact();

        List<BigDecimal> valores = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            valores.add(i < centavosRestantes ? base.add(UM_CENTAVO) : base);
        }
        return valores;
    }

    private List<BigDecimal> valoresPersonalizados(List<DivisaoRequest> pedidos, BigDecimal total) {
        List<BigDecimal> valores = new ArrayList<>();
        BigDecimal soma = BigDecimal.ZERO;

        for (DivisaoRequest pedido : pedidos) {
            if (pedido.valor() == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Informe o valor de cada morador na divisão personalizada");
            }
            BigDecimal valor = pedido.valor().setScale(2, RoundingMode.UNNECESSARY);
            valores.add(valor);
            soma = soma.add(valor);
        }

        if (soma.compareTo(total) != 0) {
            throw erro(HttpStatus.BAD_REQUEST, "A soma das divisões deve ser igual ao valor total");
        }
        return valores;
    }

    private Morador buscarMorador(Moradia moradia, Long moradorId) {
        return moradorRepository.findByIdAndMoradiaIdAndAtivoTrue(moradorId, moradia.getId())
                .orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, "Morador inválido para esta moradia"));
    }

    private String textoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private DespesaResponse montar(Despesa despesa) {
        List<DivisaoResponse> divisoes = despesa.getDivisoes().stream()
                .sorted(Comparator.comparing((DespesaDivisao d) -> d.getMorador().getNome()))
                .map(d -> new DivisaoResponse(
                        d.getMorador().getId(),
                        d.getMorador().getNome(),
                        d.getMorador().getCor(),
                        d.getValorDevido()))
                .toList();

        Morador pagoPor = despesa.getPagoPor();

        return new DespesaResponse(
                despesa.getId(),
                despesa.getNome(),
                despesa.getCategoria().getId(),
                despesa.getCategoria().getNome(),
                despesa.getValorTotal(),
                despesa.getDataDespesa(),
                despesa.getDescricao(),
                pagoPor.getId(),
                pagoPor.getNome(),
                pagoPor.getCor(),
                despesa.getTipoDivisao(),
                divisoes
        );
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}