package br.com.morajunto.morajunto_api.entrada;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.moradia.MoradiaRepository;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import br.com.morajunto.morajunto_api.usuario.Usuario;
import br.com.morajunto.morajunto_api.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final SolicitacaoEntradaRepository solicitacaoRepository;
    private final MoradiaRepository moradiaRepository;
    private final MoradorRepository moradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MoradiaAtivaService moradiaAtivaService;
    private final EntradaService entradaService;

    @Transactional(readOnly = true)
    public List<MoradiaDisponivelResponse> listarDisponiveis(Long usuarioId) {
        return moradiaRepository.findDisponiveisPara(usuarioId).stream()
                .map(moradia -> new MoradiaDisponivelResponse(
                        moradia.getId(),
                        moradia.getNome(),
                        moradia.getEndereco(),
                        moradorRepository.countByMoradiaIdAndAtivoTrue(moradia.getId()),
                        moradia.getVagas(),
                        solicitacaoRepository.existsByMoradiaIdAndUsuarioIdAndTipoAndStatus(
                                moradia.getId(), usuarioId, TipoSolicitacao.PEDIDO, StatusSolicitacao.PENDENTE)
                ))
                .toList();
    }

    @Transactional
    public SolicitacaoResponse pedir(Long usuarioId, PedidoRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Moradia moradia = moradiaRepository.findById(request.moradiaId())
                .filter(Moradia::isDisponivelParaEntrada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moradia não encontrada"));

        if (moradia.getVagas() != null && moradia.getVagas() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta moradia não tem mais vagas");
        }
        if (moradorRepository.existsByMoradiaIdAndUsuarioIdAndAtivoTrue(moradia.getId(), usuarioId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já faz parte desta moradia");
        }
        if (solicitacaoRepository.existsByMoradiaIdAndUsuarioIdAndTipoAndStatus(
                moradia.getId(), usuarioId, TipoSolicitacao.PEDIDO, StatusSolicitacao.PENDENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já tem um pedido pendente para esta moradia");
        }

        SolicitacaoEntrada pedido = new SolicitacaoEntrada();
        pedido.setMoradia(moradia);
        pedido.setUsuario(usuario);
        pedido.setTipo(TipoSolicitacao.PEDIDO);

        return SolicitacaoResponse.from(solicitacaoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarEnviados(Long usuarioId) {
        return solicitacaoRepository.findPedidosDoUsuario(usuarioId, TipoSolicitacao.PEDIDO).stream()
                .map(SolicitacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarRecebidos(Long usuarioId) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);

        if (!moradia.getDono().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o dono pode ver os pedidos de entrada");
        }

        return solicitacaoRepository
                .findPedidosPendentesDaMoradia(moradia.getId(), TipoSolicitacao.PEDIDO, StatusSolicitacao.PENDENTE)
                .stream()
                .map(SolicitacaoResponse::from)
                .toList();
    }

    @Transactional
    public SolicitacaoResponse aceitar(Long usuarioId, Long pedidoId) {
        SolicitacaoEntrada pedido = buscarPedidoPendenteDoDono(usuarioId, pedidoId);
        Moradia moradia = pedido.getMoradia();
        Usuario solicitante = pedido.getUsuario();

        entradaService.entrar(moradia, solicitante);

        if (solicitante.getMoradiaAtivaId() == null) {
            solicitante.setMoradiaAtivaId(moradia.getId());
        }

        pedido.setStatus(StatusSolicitacao.ACEITA);
        pedido.setRespondidaEm(LocalDateTime.now());
        return SolicitacaoResponse.from(pedido);
    }

    @Transactional
    public SolicitacaoResponse recusar(Long usuarioId, Long pedidoId) {
        SolicitacaoEntrada pedido = buscarPedidoPendenteDoDono(usuarioId, pedidoId);
        pedido.setStatus(StatusSolicitacao.RECUSADA);
        pedido.setRespondidaEm(LocalDateTime.now());
        return SolicitacaoResponse.from(pedido);
    }

    private SolicitacaoEntrada buscarPedidoPendenteDoDono(Long usuarioId, Long pedidoId) {
        SolicitacaoEntrada pedido = solicitacaoRepository.findPorId(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        boolean ehPedidoDoDono = pedido.getTipo() == TipoSolicitacao.PEDIDO
                && pedido.getMoradia().getDono().getId().equals(usuarioId);

        if (!ehPedidoDoDono) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
        }
        if (pedido.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este pedido já foi respondido");
        }
        return pedido;
    }
}