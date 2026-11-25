package br.com.morajunto.morajunto_api.usuario;

import br.com.morajunto.morajunto_api.auth.UsuarioResponse;
import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final MoradorRepository moradorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long usuarioId) {
        return UsuarioResponse.from(buscarUsuario(usuarioId));
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(Long usuarioId, PerfilRequest request) {
        Usuario usuario = buscarUsuario(usuarioId);

        String nome = request.nome().trim();
        String email = request.email().trim().toLowerCase();

        if (!email.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso");
        }

        String foto = request.fotoUrl();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setFotoUrl(foto == null || foto.isBlank() ? null : foto.trim());

        for (Morador morador : moradorRepository.findAtivosDoUsuario(usuarioId)) {
            morador.setNome(nome);
            morador.setEmail(email);
        }

        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void trocarSenha(Long usuarioId, TrocaSenhaRequest request) {
        Usuario usuario = buscarUsuario(usuarioId);

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha atual está incorreta");
        }
        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nova senha precisa ser diferente da atual");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}