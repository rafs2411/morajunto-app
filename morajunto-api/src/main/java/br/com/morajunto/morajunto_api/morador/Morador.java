package br.com.morajunto.morajunto_api.morador;

import br.com.morajunto.morajunto_api.moradia.Moradia;
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

import java.time.LocalDate;

@Entity
@Table(name = "moradores")
@Getter
@Setter
@NoArgsConstructor
public class Morador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moradia_id", nullable = false)
    private Moradia moradia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 160)
    private String email;

    @Column(nullable = false, length = 7, columnDefinition = "char(7)")
    private String cor;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "entrou_em", nullable = false)
    private LocalDate entrouEm;

    @Column(name = "saiu_em")
    private LocalDate saiuEm;
}