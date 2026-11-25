package br.com.morajunto.morajunto_api.config;

import java.util.Map;

public record ErroResponse(
        int status,
        String mensagem,
        Map<String, String> erros
) {
}