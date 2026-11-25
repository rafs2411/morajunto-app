package br.com.morajunto.morajunto_api.moradia;

import br.com.morajunto.morajunto_api.usuario.Usuario;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "moradias")
@Getter
@Setter
@NoArgsConstructor
public class Moradia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dono_id", nullable = false)
    private Usuario dono;

    @Column(name = "disponivel_para_entrada", nullable = false)
    private boolean disponivelParaEntrada;

    private Short vagas;

    @CreationTimestamp
    @Column(name = "criada_em", updatable = false)
    private LocalDateTime criadaEm;

    @UpdateTimestamp
    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;
}