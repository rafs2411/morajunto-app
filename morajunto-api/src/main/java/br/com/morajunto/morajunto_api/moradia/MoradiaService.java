package br.com.morajunto.morajunto_api.moradia;

import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import br.com.morajunto.morajunto_api.usuario.Usuario;
import br.com.morajunto.morajunto_api.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoradiaService {

    private static final String COR_PADRAO = "#FF5A7E";

    private final MoradiaRepository moradiaRepository;
    private final MoradorRepository moradorRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MoradiaResponse criar(Long usuarioId, MoradiaRequest request) {
        Usuario usuario = buscarUsuario(usuarioId);

        Moradia moradia = new Moradia();
        aplicar(moradia, request);
        moradia.setDono(usuario);
        Moradia salva = moradiaRepository.save(moradia);

        Morador morador = new Morador();
        morador.setMoradia(salva);
        morador.setUsuario(usuario);
        morador.setNome(usuario.getNome());
        morador.setEmail(usuario.getEmail());
        morador.setCor(COR_PADRAO);
        morador.setEntrouEm(LocalDate.now());
        moradorRepository.save(morador);

        usuario.setMoradiaAtivaId(salva.getId());

        return montar(salva, true);
    }

    @Transactional(readOnly = true)
    public List<MoradiaResponse> listar(Long usuarioId) {
        Long ativaId = buscarUsuario(usuarioId).getMoradiaAtivaId();

        return moradorRepository.findAtivosDoUsuario(usuarioId).stream()
                .map(Morador::getMoradia)
                .map(moradia -> montar(moradia, moradia.getId().equals(ativaId)))
                .toList();
    }

    @Transactional
    public MoradiaResponse selecionar(Long usuarioId, Long moradiaId) {
        Moradia moradia = buscarComoMorador(usuarioId, moradiaId);
        buscarUsuario(usuarioId).setMoradiaAtivaId(moradiaId);
        return montar(moradia, true);
    }

    @Transactional
    public MoradiaResponse atualizar(Long usuarioId, Long moradiaId, MoradiaRequest request) {
        Moradia moradia = buscarComoDono(usuarioId, moradiaId);
        aplicar(moradia, request);
        boolean ativa = moradiaId.equals(buscarUsuario(usuarioId).getMoradiaAtivaId());
        return montar(moradia, ativa);
    }

    @Transactional
    public void excluir(Long usuarioId, Long moradiaId) {
        Moradia moradia = buscarComoDono(usuarioId, moradiaId);
        moradiaRepository.delete(moradia);
    }

    private void aplicar(Moradia moradia, MoradiaRequest request) {
        boolean disponivel = Boolean.TRUE.equals(request.disponivelParaEntrada());
        moradia.setNome(request.nome().trim());
        moradia.setEndereco(request.endereco().trim());
        moradia.setDisponivelParaEntrada(disponivel);
        moradia.setVagas(disponivel ? request.vagas() : null);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private Moradia buscarComoMorador(Long usuarioId, Long moradiaId) {
        Moradia moradia = moradiaRepository.findById(moradiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moradia não encontrada"));

        if (!moradorRepository.existsByMoradiaIdAndUsuarioIdAndAtivoTrue(moradiaId, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não faz parte desta moradia");
        }
        return moradia;
    }

    private Moradia buscarComoDono(Long usuarioId, Long moradiaId) {
        Moradia moradia = buscarComoMorador(usuarioId, moradiaId);

        if (!moradia.getDono().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o dono pode alterar esta moradia");
        }
        return moradia;
    }

    private MoradiaResponse montar(Moradia moradia, boolean ativa) {
        long total = moradorRepository.countByMoradiaIdAndAtivoTrue(moradia.getId());
        return MoradiaResponse.of(moradia, total, ativa);
    }
}