package br.com.morajunto.morajunto_api.despesa;

import br.com.morajunto.morajunto_api.morador.Morador;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "despesa_divisoes")
@Getter
@Setter
@NoArgsConstructor
public class DespesaDivisao {

    @EmbeddedId
    private DespesaDivisaoId id = new DespesaDivisaoId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("despesaId")
    @JoinColumn(name = "despesa_id")
    private Despesa despesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("moradorId")
    @JoinColumn(name = "morador_id")
    private Morador morador;

    @Column(name = "valor_devido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDevido;
}