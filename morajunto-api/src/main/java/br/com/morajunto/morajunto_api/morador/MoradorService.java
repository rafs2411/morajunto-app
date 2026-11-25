package br.com.morajunto.morajunto_api.morador;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.moradia.MoradiaRepository;
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
public class MoradorService {

    private final MoradorRepository moradorRepository;
    private final MoradiaRepository moradiaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<MoradorResponse> listar(Long usuarioId) {
        Moradia moradia = moradiaAtivaDoMembro(usuarioId);
        return moradorRepository.findByMoradiaIdAndAtivoTrueOrderByNomeAsc(moradia.getId()).stream()
                .map(MoradorResponse::from)
                .toList();
    }

    @Transactional
    public MoradorResponse adicionar(Long usuarioId, MoradorRequest request) {
        Moradia moradia = moradiaAtivaDoDono(usuarioId);
        String cor = request.cor().toUpperCase();

        if (moradorRepository.existsByMoradiaIdAndCor(moradia.getId(), cor)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta cor já está em uso nesta moradia");
        }

        Morador morador = new Morador();
        morador.setMoradia(moradia);
        morador.setNome(request.nome().trim());
        morador.setEmail(normalizarEmail(request.email()));
        morador.setCor(cor);
        morador.setEntrouEm(LocalDate.now());

        return MoradorResponse.from(moradorRepository.save(morador));
    }

    @Transactional
    public MoradorResponse atualizar(Long usuarioId, Long moradorId, MoradorRequest request) {
        Moradia moradia = moradiaAtivaDoDono(usuarioId);
        Morador morador = buscarMorador(moradia, moradorId);
        String cor = request.cor().toUpperCase();

        if (moradorRepository.existsByMoradiaIdAndCorAndIdNot(moradia.getId(), cor, moradorId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta cor já está em uso nesta moradia");
        }

        morador.setNome(request.nome().trim());
        morador.setEmail(normalizarEmail(request.email()));
        morador.setCor(cor);

        return MoradorResponse.from(morador);
    }

    @Transactional
    public void remover(Long usuarioId, Long moradorId) {
        Moradia moradia = moradiaAtivaDoDono(usuarioId);
        Morador morador = buscarMorador(moradia, moradorId);

        if (morador.getUsuario() != null && morador.getUsuario().getId().equals(moradia.getDono().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O dono não pode ser removido da moradia");
        }

        morador.setAtivo(false);
        morador.setSaiuEm(LocalDate.now());
    }

    private Morador buscarMorador(Moradia moradia, Long moradorId) {
        return moradorRepository.findByIdAndMoradiaIdAndAtivoTrue(moradorId, moradia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador não encontrado"));
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private Moradia moradiaAtiva(Long usuarioId) {
        Long moradiaId = usuarioRepository.findById(usuarioId)
                .map(Usuario::getMoradiaAtivaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (moradiaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma moradia primeiro");
        }

        return moradiaRepository.findById(moradiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moradia não encontrada"));
    }

    private Moradia moradiaAtivaDoMembro(Long usuarioId) {
        Moradia moradia = moradiaAtiva(usuarioId);

        if (!moradorRepository.existsByMoradiaIdAndUsuarioIdAndAtivoTrue(moradia.getId(), usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não faz parte desta moradia");
        }
        return moradia;
    }

    private Moradia moradiaAtivaDoDono(Long usuarioId) {
        Moradia moradia = moradiaAtivaDoMembro(usuarioId);

        if (!moradia.getDono().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o dono pode alterar os moradores");
        }
        return moradia;
    }
}