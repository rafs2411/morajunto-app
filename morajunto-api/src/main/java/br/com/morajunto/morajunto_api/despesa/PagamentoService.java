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

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final MoradorRepository moradorRepository;
    private final MoradiaAtivaService moradiaAtivaService;

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listar(Long usuarioId) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        return pagamentoRepository.findByMoradiaIdOrderByDataPagamentoDescIdDesc(moradia.getId()).stream()
                .map(this::montar)
                .toList();
    }

    @Transactional
    public PagamentoResponse registrar(Long usuarioId, PagamentoRequest request) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);

        if (request.pagadorId().equals(request.recebedorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quem paga e quem recebe devem ser pessoas diferentes");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setMoradia(moradia);
        pagamento.setPagador(buscarMorador(moradia, request.pagadorId()));
        pagamento.setRecebedor(buscarMorador(moradia, request.recebedorId()));
        pagamento.setValor(request.valor().setScale(2, RoundingMode.UNNECESSARY));
        pagamento.setDataPagamento(request.dataPagamento() != null ? request.dataPagamento() : LocalDate.now());
        pagamento.setObservacao(textoOpcional(request.observacao()));

        return montar(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public void excluir(Long usuarioId, Long pagamentoId) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);
        Pagamento pagamento = pagamentoRepository.findByIdAndMoradiaId(pagamentoId, moradia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));
        pagamentoRepository.delete(pagamento);
    }

    private Morador buscarMorador(Moradia moradia, Long moradorId) {
        return moradorRepository.findByIdAndMoradiaId(moradorId, moradia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Morador inválido para esta moradia"));
    }

    private String textoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private PagamentoResponse montar(Pagamento pagamento) {
        Morador pagador = pagamento.getPagador();
        Morador recebedor = pagamento.getRecebedor();

        return new PagamentoResponse(
                pagamento.getId(),
                pagador.getId(),
                pagador.getNome(),
                pagador.getCor(),
                recebedor.getId(),
                recebedor.getNome(),
                recebedor.getCor(),
                pagamento.getValor(),
                pagamento.getDataPagamento(),
                pagamento.getObservacao()
        );
    }
}