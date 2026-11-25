package br.com.morajunto.morajunto_api.entrada;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.morador.Morador;
import br.com.morajunto.morajunto_api.morador.MoradorRepository;
import br.com.morajunto.morajunto_api.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntradaService {

    private static final List<String> PALETA = List.of(
            "#FF5A7E", "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6",
            "#EF4444", "#14B8A6", "#F97316", "#EC4899", "#6366F1"
    );

    private final MoradorRepository moradorRepository;

    public void entrar(Moradia moradia, Usuario usuario) {
        Optional<Morador> anterior = moradorRepository.findByMoradiaIdAndUsuarioId(moradia.getId(), usuario.getId());

        if (anterior.isPresent()) {
            Morador morador = anterior.get();
            if (morador.isAtivo()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta pessoa já faz parte desta moradia");
            }
            consumirVaga(moradia);
            morador.setAtivo(true);
            morador.setSaiuEm(null);
            morador.setEntrouEm(LocalDate.now());
            return;
        }

        Optional<Morador> semConta = moradorRepository
                .findFirstByMoradiaIdAndEmailAndUsuarioIsNullAndAtivoTrue(moradia.getId(), usuario.getEmail());

        if (semConta.isPresent()) {
            semConta.get().setUsuario(usuario);
            return;
        }

        consumirVaga(moradia);

        Morador novo = new Morador();
        novo.setMoradia(moradia);
        novo.setUsuario(usuario);
        novo.setNome(usuario.getNome());
        novo.setEmail(usuario.getEmail());
        novo.setCor(corLivre(moradia.getId()));
        novo.setEntrouEm(LocalDate.now());
        moradorRepository.save(novo);
    }

    private void consumirVaga(Moradia moradia) {
        Short vagas = moradia.getVagas();
        if (vagas == null) {
            return;
        }
        if (vagas <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta moradia não tem mais vagas");
        }
        moradia.setVagas((short) (vagas - 1));
    }

    private String corLivre(Long moradiaId) {
        return PALETA.stream()
                .filter(cor -> !moradorRepository.existsByMoradiaIdAndCor(moradiaId, cor))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Não há cores livres nesta moradia"));
    }
}