package br.com.morajunto.morajunto_api.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "preferencias_usuario")
@Getter
@Setter
@NoArgsConstructor
public class PreferenciasUsuario {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tema tema = Tema.CLARO;

    @Column(nullable = false, length = 10)
    private String idioma = "pt-BR";

    @Column(name = "notificar_tarefas", nullable = false)
    private boolean notificarTarefas = true;

    @Column(name = "notificar_despesas", nullable = false)
    private boolean notificarDespesas = true;
}