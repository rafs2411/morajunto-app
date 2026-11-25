package br.com.morajunto.morajunto_api.despesa;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.morador.Morador;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "despesas")
@Getter
@Setter
@NoArgsConstructor
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moradia_id", nullable = false)
    private Moradia moradia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaDespesa categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pago_por_morador_id", nullable = false)
    private Morador pagoPor;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_despesa", nullable = false)
    private LocalDate dataDespesa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_divisao", nullable = false)
    private TipoDivisao tipoDivisao = TipoDivisao.IGUAL;

    @OneToMany(mappedBy = "despesa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DespesaDivisao> divisoes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criada_em", updatable = false)
    private LocalDateTime criadaEm;

    @UpdateTimestamp
    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;
}