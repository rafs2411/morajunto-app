package br.com.morajunto.morajunto_api.entrada;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaAtivaService;
import br.com.morajunto.morajunto_api.moradia.MoradiaRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private SolicitacaoEntradaRepository solicitacaoRepository;

    @Mock
    private MoradiaRepository moradiaRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MoradiaAtivaService moradiaAtivaService;

    @Mock
    private EntradaService entradaService;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario dono;
    private Usuario lucas;
    private Moradia moradia;

    @BeforeEach
    void preparar() {
        dono = usuario(1L, "Maria Santos", "maria@email.com");
        lucas = usuario(3L, "Lucas Rocha", "lucas@email.com");

        moradia = new Moradia();
        moradia.setId(1L);
        moradia.setNome("Casa da Galera");
        moradia.setEndereco("Rua das Palmeiras, 127");
        moradia.setDono(dono);
        moradia.setDisponivelParaEntrada(true);
        moradia.setVagas((short) 2);
    }

    @Test
    @DisplayName("Pedido para moradia fechada devolve 404")
    void pedidoParaMoradiaFechada() {
        moradia.setDisponivelParaEntrada(false);
        prepararPedido();

        assertThatThrownBy(() -> pedidoService.pedir(3L, new PedidoRequest(1L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(404));

        verify(solicitacaoRepository, never()).save(any(SolicitacaoEntrada.class));
    }

    @Test
    @DisplayName("Pedido para moradia sem vagas devolve 409")
    void pedidoParaMoradiaSemVagas() {
        moradia.setVagas((short) 0);
        prepararPedido();

        assertThatThrownBy(() -> pedidoService.pedir(3L, new PedidoRequest(1L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(409));

        verify(solicitacaoRepository, never()).save(any(SolicitacaoEntrada.class));
    }

    @Test
    @DisplayName("Quem já mora na moradia não pode pedir para entrar")
    void quemJaMoraNaoPede() {
        prepararPedido();
        when(moradorRepository.existsByMoradiaIdAndUsuarioIdAndAtivoTrue(1L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> pedidoService.pedir(3L, new PedidoRequest(1L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(409));

        verify(solicitacaoRepository, never()).save(any(SolicitacaoEntrada.class));
    }

    @Test
    @DisplayName("Não cria um segundo pedido pendente para a mesma moradia")
    void naoDuplicaPedidoPendente() {
        prepararPedido();
        when(solicitacaoRepository.existsByMoradiaIdAndUsuarioIdAndTipoAndStatus(
                1L, 3L, TipoSolicitacao.PEDIDO, StatusSolicitacao.PENDENTE)).thenReturn(true);

        assertThatThrownBy(() -> pedidoService.pedir(3L, new PedidoRequest(1L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(409));

        verify(solicitacaoRepository, never()).save(any(SolicitacaoEntrada.class));
    }

    @Test
    @DisplayName("Pedido válido fica pendente, sem prazo e sem gastar vaga")
    void pedidoValido() {
        prepararPedido();
        when(solicitacaoRepository.save(any(SolicitacaoEntrada.class))).thenAnswer(chamada -> chamada.getArgument(0));

        SolicitacaoResponse resposta = pedidoService.pedir(3L, new PedidoRequest(1L));

        assertThat(resposta.tipo()).isEqualTo(TipoSolicitacao.PEDIDO);
        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.PENDENTE);
        assertThat(resposta.usuarioId()).isEqualTo(3L);
        assertThat(resposta.expiraEm()).isNull();
        assertThat(moradia.getVagas()).isEqualTo((short) 2);
    }

    @Test
    @DisplayName("Só o dono da moradia pode aceitar um pedido")
    void soODonoAceita() {
        when(solicitacaoRepository.findPorId(7L)).thenReturn(Optional.of(pedidoPendente()));

        assertThatThrownBy(() -> pedidoService.aceitar(2L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(404));

        verify(entradaService, never()).entrar(any(), any());
    }

    @Test
    @DisplayName("Pedido aceito coloca a pessoa na moradia e deixa a moradia ativa para ela")
    void pedidoAceito() {
        SolicitacaoEntrada pedido = pedidoPendente();
        when(solicitacaoRepository.findPorId(7L)).thenReturn(Optional.of(pedido));

        SolicitacaoResponse resposta = pedidoService.aceitar(1L, 7L);

        verify(entradaService).entrar(moradia, lucas);
        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.ACEITA);
        assertThat(pedido.getRespondidaEm()).isNotNull();
        assertThat(lucas.getMoradiaAtivaId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Pedido aceito não troca a moradia ativa de quem já tinha uma")
    void pedidoAceitoMantemMoradiaAtiva() {
        lucas.setMoradiaAtivaId(9L);
        when(solicitacaoRepository.findPorId(7L)).thenReturn(Optional.of(pedidoPendente()));

        pedidoService.aceitar(1L, 7L);

        assertThat(lucas.getMoradiaAtivaId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("Pedido recusado não coloca ninguém na moradia")
    void pedidoRecusado() {
        when(solicitacaoRepository.findPorId(7L)).thenReturn(Optional.of(pedidoPendente()));

        SolicitacaoResponse resposta = pedidoService.recusar(1L, 7L);

        assertThat(resposta.status()).isEqualTo(StatusSolicitacao.RECUSADA);
        verify(entradaService, never()).entrar(any(), any());
    }

    @Test
    @DisplayName("Quem não é dono não vê os pedidos recebidos")
    void naoDonoNaoVePedidos() {
        when(moradiaAtivaService.doMembro(3L)).thenReturn(moradia);

        assertThatThrownBy(() -> pedidoService.listarRecebidos(3L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(status(erro)).isEqualTo(403));
    }

    private void prepararPedido() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(lucas));
        when(moradiaRepository.findById(1L)).thenReturn(Optional.of(moradia));
    }

    private SolicitacaoEntrada pedidoPendente() {
        SolicitacaoEntrada pedido = new SolicitacaoEntrada();
        pedido.setId(7L);
        pedido.setMoradia(moradia);
        pedido.setUsuario(lucas);
        pedido.setTipo(TipoSolicitacao.PEDIDO);
        pedido.setStatus(StatusSolicitacao.PENDENTE);
        return pedido;
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