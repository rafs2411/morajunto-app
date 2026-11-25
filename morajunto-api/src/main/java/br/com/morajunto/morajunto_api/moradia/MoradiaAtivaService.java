package br.com.morajunto.morajunto_api.moradia;

import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import br.com.morajunto.morajunto_api.usuario.Usuario;
import br.com.morajunto.morajunto_api.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MoradiaAtivaService {

    private final UsuarioRepository usuarioRepository;
    private final MoradiaRepository moradiaRepository;
    private final MoradorRepository moradorRepository;

    @Transactional(readOnly = true)
    public Moradia doMembro(Long usuarioId) {
        Long moradiaId = usuarioRepository.findById(usuarioId)
                .map(Usuario::getMoradiaAtivaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (moradiaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma moradia primeiro");
        }

        Moradia moradia = moradiaRepository.findById(moradiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moradia não encontrada"));

        if (!moradorRepository.existsByMoradiaIdAndUsuarioIdAndAtivoTrue(moradiaId, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não faz parte desta moradia");
        }
        return moradia;
    }
}