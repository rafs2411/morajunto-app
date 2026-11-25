package br.com.morajunto.morajunto_api.tarefa;

import br.com.morajunto.morajunto_api.morador.Morador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarefa_ocorrencias")
@Getter
@Setter
@NoArgsConstructor
public class TarefaOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDate dataOcorrencia;

    @Column(nullable = false)
    private boolean concluida;

    @Column(name = "concluida_em")
    private LocalDateTime concluidaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concluida_por_morador_id")
    private Morador concluidaPor;
}