package br.com.morajunto.morajunto_api.despesa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DespesaDivisaoId implements Serializable {

    @Column(name = "despesa_id")
    private Long despesaId;

    @Column(name = "morador_id")
    private Long moradorId;
}