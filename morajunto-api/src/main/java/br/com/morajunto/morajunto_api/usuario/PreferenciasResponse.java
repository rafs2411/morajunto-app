package br.com.morajunto.morajunto_api.usuario;

public record PreferenciasResponse(
        Tema tema,
        String idioma,
        boolean notificarTarefas,
        boolean notificarDespesas
) {

    public static PreferenciasResponse from(PreferenciasUsuario preferencias) {
        return new PreferenciasResponse(
                preferencias.getTema(),
                preferencias.getIdioma(),
                preferencias.isNotificarTarefas(),
                preferencias.isNotificarDespesas()
        );
    }
}