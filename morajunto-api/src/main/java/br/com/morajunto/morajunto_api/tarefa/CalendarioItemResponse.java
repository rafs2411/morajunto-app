package br.com.morajunto.morajunto_api.tarefa;

import java.time.LocalDate;
import java.time.LocalTime;

public record CalendarioItemResponse(
        Long tarefaId,
        String nome,
        String descricao,
        LocalDate data,
        LocalTime horario,
        Long responsavelId,
        String responsavelNome,
        String responsavelCor,
        boolean concluida,
        Long concluidaPorId,
        String concluidaPorNome
) {
}