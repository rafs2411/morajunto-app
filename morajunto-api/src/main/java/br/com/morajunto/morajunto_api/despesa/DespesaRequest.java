package br.com.morajunto.morajunto_api.despesa;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DespesaRequest(
        @NotBlank(message = "Informe o nome da despesa")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres")
        String nome,

        @NotNull(message = "Informe a categoria")
        Short categoriaId,

        @NotNull(message = "Informe o valor total")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Valor inválido")
        BigDecimal valorTotal,

        @NotNull(message = "Informe a data")
        LocalDate dataDespesa,

        String descricao,

        @NotNull(message = "Informe quem pagou")
        Long pagoPorMoradorId,

        TipoDivisao tipoDivisao,

        @NotEmpty(message = "Informe entre quem a despesa será dividida")
        List<@Valid DivisaoRequest> divisoes
) {
}