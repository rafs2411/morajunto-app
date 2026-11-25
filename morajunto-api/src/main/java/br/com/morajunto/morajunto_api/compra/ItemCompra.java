package br.com.morajunto.morajunto_api.compra;

import br.com.morajunto.morajunto_api.moradia.Moradia;
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
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "itens_compra")
@Getter
@Setter
@NoArgsConstructor
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moradia_id", nullable = false)
    private Moradia moradia;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 40)
    private String quantidade;

    @Column(nullable = false)
    private boolean comprado;

    @Column(name = "comprado_em")
    private LocalDateTime compradoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprado_por_morador_id")
    private Morador compradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_morador_id")
    private Morador criadoPor;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}