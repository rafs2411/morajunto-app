package br.com.morajunto.morajunto_api.tarefa;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record TarefaRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @NotNull(message = "O responsável é obrigatório")
        Long responsavelId,

        @NotNull(message = "A recorrência é obrigatória")
        Recorrencia recorrencia,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate dataInicio,

        LocalDate dataFim,

        LocalTime horario,

        Set<@Min(value = 0, message = "O dia da semana deve ser de 0 a 6")
        @Max(value = 6, message = "O dia da semana deve ser de 0 a 6") Integer> diasSemana
) {
}