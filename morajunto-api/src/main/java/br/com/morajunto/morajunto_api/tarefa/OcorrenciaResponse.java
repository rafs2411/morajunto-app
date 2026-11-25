package br.com.morajunto.morajunto_api.tarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OcorrenciaResponse(
        Long tarefaId,
        LocalDate data,
        boolean concluida,
        LocalDateTime concluidaEm,
        Long concluidaPorId,
        String concluidaPorNome
) {
}