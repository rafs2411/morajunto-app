package br.com.morajunto.morajunto_api.tarefa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TarefaResponse(
        Long id,
        String nome,
        String descricao,
        Long responsavelId,
        String responsavelNome,
        String responsavelCor,
        Recorrencia recorrencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horario,
        List<Integer> diasSemana
) {
}