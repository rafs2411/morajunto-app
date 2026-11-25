package br.com.morajunto.morajunto_api.dashboard;

import br.com.morajunto.morajunto_api.compra.CompraService;
import br.com.morajunto.morajunto_api.compra.ItemCompraResponse;
import br.com.morajunto.morajunto_api.despesa.Despesa;
import br.com.morajunto.morajunto_api.despesa.DespesaRepository;
import br.com.morajunto.morajunto_api.despesa.SaldoService;
import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import br.com.morajunto.morajunto_api.tarefa.CalendarioItemResponse;
import br.com.morajunto.morajunto_api.tarefa.StatusTarefa;
import br.com.morajunto.morajunto_api.tarefa.TarefaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int PREVIA_COMPRAS = 5;

    private final MoradiaAtivaService moradiaAtivaService;
    private final DespesaRepository despesaRepository;
    private final MoradorRepository moradorRepository;
    private final SaldoService saldoService;
    private final TarefaService tarefaService;
    private final CompraService compraService;

    @Transactional(readOnly = true)
    public DashboardResponse montar(Long usuarioId) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);

        LocalDate hoje = LocalDate.now();
        YearMonth mes = YearMonth.from(hoje);

        List<Despesa> despesas = despesaRepository.findDoPeriodo(moradia.getId(), mes.atDay(1), mes.atEndOfMonth());
        BigDecimal totalDespesas = despesas.stream()
                .map(Despesa::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);

        int tarefasPendentes = tarefaService
                .calendario(usuarioId, mes.atDay(1), mes.atEndOfMonth(), StatusTarefa.PENDENTES)
                .size();

        List<CalendarioItemResponse> tarefasHoje = tarefaService
                .calendario(usuarioId, hoje, hoje, StatusTarefa.TODAS);

        List<ItemCompraResponse> paraComprar = compraService.listar(usuarioId).paraComprar();

        return new DashboardResponse(
                mes.toString(),
                totalDespesas,
                despesas.size(),
                tarefasPendentes,
                paraComprar.size(),
                moradorRepository.countByMoradiaIdAndAtivoTrue(moradia.getId()),
                saldoService.resumo(usuarioId),
                tarefasHoje,
                paraComprar.stream().limit(PREVIA_COMPRAS).toList()
        );
    }
}