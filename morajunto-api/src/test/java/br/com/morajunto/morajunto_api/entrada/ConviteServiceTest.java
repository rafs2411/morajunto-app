package br.com.morajunto.morajunto_api.entrada;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.moradia.MoradiaResponse;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import br.com.morajunto.morajunto_api.usuario.Usuario;
import br.com.morajunto.morajunto_api.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConviteServiceTest {

    @Mock
    private SolicitacaoEntradaRepository solicitacaoRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MoradiaAtivaService moradiaAtivaService;

    @Mock
    private EntradaService entradaService;

    @InjectMocks
    private ConviteService conviteService;

    private Usuario dono;
    private Usuario convidado;
    private Moradia moradia;

    @BeforeEach
    void preparar() {
        dono = usuario(1L, "Maria Santos", "maria@email.com");
        convidado = usuario(2L, "Pedro Lima", "pedro@email.com");

        moradia = new Moradia();
        moradia.setId(1L);
        moradia.setNome("Casa da Galera");
        moradia.setEndereco("Rua das Palmeiras, 127");
        moradia.setDono(dono);
    }

    @Test
    @DisplayName("Só o dono pode convidar")
    void soODonoConvida() {
        when(moradiaAtivaService.doMembro(2L)).thenReturn(moradia);

        assertThatThrownBy(() -> conviteService.convidar(2L, new ConviteRequest("ana@email.com")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(403));

        verify(solicitacaoRepository, never()).save(any(SolicitacaoEntrada.class));
    }

    @Test
    @DisplayName("Não cria um segundo convite pendente para o mesmo e-mail")
    void naoDuplicaConvitePendente() {
        SolicitacaoEntrada pendente = convite("pedro@email.com", LocalDateTime.now().plusDays(3));
        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);
        when(solicitacaoRepository.findByMoradiaIdAndTipoAndStatusAndEmailConvidado(
                1L, TipoSolicitacao.CONVITE, StatusSolicitacao.PENDENTE, "pedro@email.com"))
                .thenReturn(List.of(pendente));

        assertThatThrownBy(() -> conviteService.convidar(1L, new ConviteRequest("pedro@email.com")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(409));

        verify(solicitacaoRepository, never()).save(any(SolicitacaoEntrada.class));
    }

    @Test
    @DisplayName("Convite vencido vira EXPIRADA e um novo é criado com o e-mail em minúsculas")
    void conviteVencidoExpiraECriaNovo() {
        SolicitacaoEntrada vencido = convite("pedro@email.com", LocalDateTime.now().minusDays(1));
        when(moradiaAtivaService.doMembro(1L)).thenReturn(moradia);
        when(solicitacaoRepository.findByMoradiaIdAndTipoAndStatusAndEmailConvidado(
                1L, TipoSolicitacao.CONVITE, StatusSolicitacao.PENDENTE, "pedro@email.com"))
                .thenReturn(List.of(vencido));
        when(solicitacaoRepository.save(any(SolicitacaoEntrada.class))).thenAnswer(chamada -> chamada.getArgument(0));

        SolicitacaoResponse resposta = conviteService.convidar(1L, new ConviteRequest("  Pedro@Email.com "));

        assertThat(vencido.getStatus()).isEqualTo(StatusSolicitacao.EXPIRADA);
        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.PENDENTE);
        assertThat(resposta.emailConvidado()).isEqualTo("pedro@email.com");
        assertThat(resposta.expiraEm()).isAfter(LocalDateTime.now().plusDays(6));
    }

    @Test
    @DisplayName("Aceitar convite vencido devolve 410 e marca como EXPIRADA")
    void aceitarConviteVencido() {
        SolicitacaoEntrada vencido = convite("pedro@email.com", LocalDateTime.now().minusMinutes(1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(convidado));
        when(solicitacaoRepository.findPorId(5L)).thenReturn(Optional.of(vencido));

        assertThatThrownBy(() -> conviteService.aceitar(2L, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(410));

        assertThat(vencido.getStatus()).isEqualTo(StatusSolicitacao.EXPIRADA);
        verify(entradaService, never()).entrar(any(), any());
    }

    @Test
    @DisplayName("Aceitar convite válido coloca a pessoa na moradia e deixa a moradia ativa")
    void aceitarConviteValido() {
        SolicitacaoEntrada valido = convite("pedro@email.com", LocalDateTime.now().plusDays(5));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(convidado));
        when(solicitacaoRepository.findPorId(5L)).thenReturn(Optional.of(valido));
        when(moradorRepository.countByMoradiaIdAndAtivoTrue(1L)).thenReturn(5L);

        MoradiaResponse resposta = conviteService.aceitar(2L, 5L);

        verify(entradaService).entrar(moradia, convidado);
        assertThat(valido.getStatus()).isEqualTo(StatusSolicitacao.ACEITA);
        assertThat(valido.getUsuario()).isSameAs(convidado);
        assertThat(valido.getRespondidaEm()).isNotNull();
        assertThat(convidado.getMoradiaAtivaId()).isEqualTo(1L);
        assertThat(resposta.totalMoradores()).isEqualTo(5L);
        assertThat(resposta.ativa()).isTrue();
    }

    @Test
    @DisplayName("Ninguém aceita convite enviado para outro e-mail")
    void naoAceitaConviteDeOutroEmail() {
        Usuario outro = usuario(3L, "Lucas Rocha", "lucas@email.com");
        SolicitacaoEntrada convite = convite("pedro@email.com", LocalDateTime.now().plusDays(5));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(outro));
        when(solicitacaoRepository.findPorId(5L)).thenReturn(Optional.of(convite));

        assertThatThrownBy(() -> conviteService.aceitar(3L, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(404));

        verify(entradaService, never()).entrar(any(), any());
    }

    @Test
    @DisplayName("Convite já respondido não pode ser recusado de novo")
    void naoRecusaConviteJaRespondido() {
        SolicitacaoEntrada aceito = convite("pedro@email.com", LocalDateTime.now().plusDays(5));
        aceito.setStatus(StatusSolicitacao.ACEITA);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(convidado));
        when(solicitacaoRepository.findPorId(5L)).thenReturn(Optional.of(aceito));

        assertThatThrownBy(() -> conviteService.recusar(2L, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(409));

        assertThat(aceito.getStatus()).isEqualTo(StatusSolicitacao.ACEITA);
    }

    private SolicitacaoEntrada convite(String email, LocalDateTime expiraEm) {
        SolicitacaoEntrada convite = new SolicitacaoEntrada();
        convite.setId(5L);
        convite.setMoradia(moradia);
        convite.setEmailConvidado(email);
        convite.setTipo(TipoSolicitacao.CONVITE);
        convite.setStatus(StatusSolicitacao.PENDENTE);
        convite.setExpiraEm(expiraEm);
        return convite;
    }

    private Usuario usuario(Long id, String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        return usuario;
    }

    private int status(Throwable erro) {
        return ((ResponseStatusException) erro).getStatusCode().value();
    }
}