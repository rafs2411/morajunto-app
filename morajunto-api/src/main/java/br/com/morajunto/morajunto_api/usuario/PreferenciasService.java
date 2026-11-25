package br.com.morajunto.morajunto_api.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferenciasService {

    private final PreferenciasUsuarioRepository preferenciasRepository;

    @Transactional(readOnly = true)
    public PreferenciasResponse buscar(Long usuarioId) {
        return PreferenciasResponse.from(carregar(usuarioId));
    }

    @Transactional
    public PreferenciasResponse atualizar(Long usuarioId, PreferenciasRequest request) {
        PreferenciasUsuario preferencias = carregar(usuarioId);
        preferencias.setTema(request.tema());
        preferencias.setIdioma(request.idioma());
        preferencias.setNotificarTarefas(request.notificarTarefas());
        preferencias.setNotificarDespesas(request.notificarDespesas());
        return PreferenciasResponse.from(preferenciasRepository.save(preferencias));
    }

    private PreferenciasUsuario carregar(Long usuarioId) {
        return preferenciasRepository.findById(usuarioId).orElseGet(() -> {
            PreferenciasUsuario nova = new PreferenciasUsuario();
            nova.setUsuarioId(usuarioId);
            return nova;
        });
    }
}