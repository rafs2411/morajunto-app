package br.com.morajunto.morajunto_api.tarefa;

import br.com.morajunto.morajunto_api.moradia.Moradia;
import br.com.morajunto.morajunto_api.morador.Morador;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tarefas")
@Getter
@Setter
@NoArgsConstructor
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moradia_id", nullable = false)
    private Moradia moradia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Morador responsavel;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recorrencia recorrencia = Recorrencia.SEMANAL;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    private boolean ativa = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tarefa_dias_semana", joinColumns = @JoinColumn(name = "tarefa_id"))
    @Column(name = "dia_semana", columnDefinition = "tinyint unsigned")
    private Set<Integer> diasSemana = new HashSet<>();

    @CreationTimestamp
    @Column(name = "criada_em", updatable = false)
    private LocalDateTime criadaEm;

    @UpdateTimestamp
    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;
}