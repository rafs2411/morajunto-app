package br.com.morajunto.morajunto_api.entrada;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.moradia.MoradiaResponse;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConviteService {

    private static final int DIAS_VALIDADE = 7;

    private final SolicitacaoEntradaRepository solicitacaoRepository;
    private final MoradorRepository moradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MoradiaAtivaService moradiaAtivaService;
    private final EntradaService entradaService;

    @Transactional
    public SolicitacaoResponse convidar(Long usuarioId, ConviteRequest request) {
        Moradia moradia = moradiaAtivaService.doMembro(usuarioId);

        if (!moradia.getDono().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o dono pode convidar pessoas");
        }

        String email = request.email().trim().toLowerCase();

        usuarioRepository.findByEmail(email).ifPresent(convidado -> {
            if (moradorRepository.existsByMoradiaIdAndUsuarioIdAndAtivoTrue(moradia.getId(), convidado.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta pessoa já mora aqui");
            }
        });

        LocalDateTime agora = LocalDateTime.now();

        List<SolicitacaoEntrada> pendentes = solicitacaoRepository
                .findByMoradiaIdAndTipoAndStatusAndEmailConvidado(
                        moradia.getId(), TipoSolicitacao.CONVITE, StatusSolicitacao.PENDENTE, email);

        for (SolicitacaoEntrada pendente : pendentes) {
            if (!expirada(pendente, agora)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um convite pendente para este e-mail");
            }
            pendente.setStatus(StatusSolicitacao.EXPIRADA);
            pendente.setRespondidaEm(agora);
        }

        SolicitacaoEntrada convite = new SolicitacaoEntrada();
        convite.setMoradia(moradia);
        convite.setEmailConvidado(email);
        convite.setTipo(TipoSolicitacao.CONVITE);
        convite.setCodigo(UUID.randomUUID().toString().replace("-", ""));
        convite.setExpiraEm(agora.plusDays(DIAS_VALIDADE));

        return SolicitacaoResponse.from(solicitacaoRepository.save(convite));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarRecebidos(Long usuarioId) {
        String email = buscarUsuario(usuarioId).getEmail();

        return solicitacaoRepository
                .findConvitesPendentesPorEmail(TipoSolicitacao.CONVITE, StatusSolicitacao.PENDENTE, email, LocalDateTime.now())
                .stream()
                .map(SolicitacaoResponse::from)
                .toList();
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public MoradiaResponse aceitar(Long usuarioId, Long conviteId) {
        Usuario usuario = buscarUsuario(usuarioId);
        SolicitacaoEntrada convite = buscarConvitePendente(usuario, conviteId);
        LocalDateTime agora = LocalDateTime.now();

        if (expirada(convite, agora)) {
            convite.setStatus(StatusSolicitacao.EXPIRADA);
            convite.setRespondidaEm(agora);
            throw new ResponseStatusException(HttpStatus.GONE, "Este convite expirou");
        }

        Moradia moradia = convite.getMoradia();
        entradaService.entrar(moradia, usuario);

        convite.setStatus(StatusSolicitacao.ACEITA);
        convite.setUsuario(usuario);
        convite.setRespondidaEm(agora);
        usuario.setMoradiaAtivaId(moradia.getId());

        long total = moradorRepository.countByMoradiaIdAndAtivoTrue(moradia.getId());
        return MoradiaResponse.of(moradia, total, true);
    }

    @Transactional
    public void recusar(Long usuarioId, Long conviteId) {
        Usuario usuario = buscarUsuario(usuarioId);
        SolicitacaoEntrada convite = buscarConvitePendente(usuario, conviteId);
        convite.setStatus(StatusSolicitacao.RECUSADA);
        convite.setRespondidaEm(LocalDateTime.now());
    }

    private SolicitacaoEntrada buscarConvitePendente(Usuario usuario, Long conviteId) {
        SolicitacaoEntrada convite = solicitacaoRepository.findPorId(conviteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado"));

        boolean ehConvitePara = convite.getTipo() == TipoSolicitacao.CONVITE
                && usuario.getEmail().equalsIgnoreCase(convite.getEmailConvidado());

        if (!ehConvitePara) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado");
        }
        if (convite.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este convite já foi respondido");
        }
        return convite;
    }

    private boolean expirada(SolicitacaoEntrada solicitacao, LocalDateTime agora) {
        return solicitacao.getExpiraEm() != null && !solicitacao.getExpiraEm().isAfter(agora);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}