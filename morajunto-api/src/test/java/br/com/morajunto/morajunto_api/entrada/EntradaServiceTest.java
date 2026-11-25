package br.com.morajunto.morajunto_api.entrada;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import br.com.morajunto.morajunto_api.usuario.Usuario;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntradaServiceTest {

    @Mock
    private MoradorRepository moradorRepository;

    @InjectMocks
    private EntradaService entradaService;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Lucas Rocha");
        usuario.setEmail("lucas@email.com");
    }

    @Test
    @DisplayName("Morador novo recebe a primeira cor livre e gasta uma vaga")
    void moradorNovoRecebeCorLivreEGastaVaga() {
        Moradia moradia = moradia((short) 2);
        when(moradorRepository.existsByMoradiaIdAndCor(1L, "#FF5A7E")).thenReturn(true);
        when(moradorRepository.existsByMoradiaIdAndCor(1L, "#3B82F6")).thenReturn(true);

        entradaService.entrar(moradia, usuario);

        ArgumentCaptor<Morador> salvo = ArgumentCaptor.forClass(Morador.class);
        verify(moradorRepository).save(salvo.capture());
        assertThat(salvo.getValue().getCor()).isEqualTo("#10B981");
        assertThat(salvo.getValue().getUsuario()).isSameAs(usuario);
        assertThat(salvo.getValue().getNome()).isEqualTo("Lucas Rocha");
        assertThat(salvo.getValue().getEmail()).isEqualTo("lucas@email.com");
        assertThat(moradia.getVagas()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("Moradia sem limite de vagas continua sem limite")
    void moradiaSemLimiteContinuaSemLimite() {
        Moradia moradia = moradia(null);

        entradaService.entrar(moradia, usuario);

        verify(moradorRepository).save(any(Morador.class));
        assertThat(moradia.getVagas()).isNull();
    }

    @Test
    @DisplayName("Moradia sem vagas recusa a entrada com 409")
    void moradiaSemVagasRecusa() {
        Moradia moradia = moradia((short) 0);

        assertThatThrownBy(() -> entradaService.entrar(moradia, usuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(erro -> assertThat(((ResponseStatusException) erro).getStatusCode().value()).isEqualTo(409));

        verify(moradorRepository, never()).save(any(Morador.class));
    }

    @Test
    @DisplayName("Quem já morou e saiu é reativado com a mesma cor e gasta vaga")
    void moradorQueSaiuEReativado() {
        Moradia moradia = moradia((short) 1);
        Morador antigo = new Morador();
        antigo.setAtivo(false);
        antigo.setSaiuEm(LocalDate.of(2026, 10, 1));
        antigo.setCor("#8B5CF6");
        when(moradorRepository.findByMoradiaIdAndUsuarioId(1L, 10L)).thenReturn(Optional.of(antigo));

        entradaService.entrar(moradia, usuario);

        assertThat(antigo.isAtivo()).isTrue();
        assertThat(antigo.getSaiuEm()).isNull();
        assertThat(antigo.getCor()).isEqualTo("#8B5CF6");
        assertThat(moradia.getVagas()).isEqualTo((short) 0);
        verify(moradorRepository, never()).save(any(Morador.class));
    }

    @Test
    @DisplayName("Quem já mora na casa não entra de novo")
    void moradorAtivoNaoEntraDeNovo() {
        Moradia moradia = moradia((short) 2);
        Morador atual = new Morador();
        atual.setAtivo(true);
        when(moradorRepository.findByMoradiaIdAndUsuarioId(1L, 10L)).thenReturn(Optional.of(atual));

        assertThatThrownBy(() -> entradaService.entrar(moradia, usuario))
                .isInstanceOf(ResponseStatusException.class);

        assertThat(moradia.getVagas()).isEqualTo((short) 2);
    }

    @Test
    @DisplayName("Morador cadastrado sem conta com o mesmo e-mail é vinculado sem gastar vaga")
    void moradorSemContaEVinculado() {
        Moradia moradia = moradia((short) 2);
        Morador semConta = new Morador();
        semConta.setAtivo(true);
        when(moradorRepository.findFirstByMoradiaIdAndEmailAndUsuarioIsNullAndAtivoTrue(1L, "lucas@email.com"))
                .thenReturn(Optional.of(semConta));

        entradaService.entrar(moradia, usuario);

        assertThat(semConta.getUsuario()).isSameAs(usuario);
        assertThat(moradia.getVagas()).isEqualTo((short) 2);
        verify(moradorRepository, never()).save(any(Morador.class));
    }

    private Moradia moradia(Short vagas) {
        Moradia moradia = new Moradia();
        moradia.setId(1L);
        moradia.setVagas(vagas);
        return moradia;
    }
}