package br.com.morajunto.morajunto_api.entrada;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoEntradaRepository extends JpaRepository<SolicitacaoEntrada, Long> {

    @Query("select s from SolicitacaoEntrada s join fetch s.moradia " +
            "where s.tipo = :tipo and s.status = :status and s.emailConvidado = :email " +
            "and (s.expiraEm is null or s.expiraEm > :agora) " +
            "order by s.criadaEm desc")
    List<SolicitacaoEntrada> findConvitesPendentesPorEmail(@Param("tipo") TipoSolicitacao tipo,
                                                           @Param("status") StatusSolicitacao status,
                                                           @Param("email") String email,
                                                           @Param("agora") LocalDateTime agora);

    @Query("select s from SolicitacaoEntrada s join fetch s.usuario " +
            "where s.moradia.id = :moradiaId and s.tipo = :tipo and s.status = :status " +
            "order by s.criadaEm")
    List<SolicitacaoEntrada> findPedidosPendentesDaMoradia(@Param("moradiaId") Long moradiaId,
                                                           @Param("tipo") TipoSolicitacao tipo,
                                                           @Param("status") StatusSolicitacao status);

    @Query("select s from SolicitacaoEntrada s join fetch s.moradia join fetch s.usuario " +
            "where s.usuario.id = :usuarioId and s.tipo = :tipo " +
            "order by s.criadaEm desc")
    List<SolicitacaoEntrada> findPedidosDoUsuario(@Param("usuarioId") Long usuarioId,
                                                  @Param("tipo") TipoSolicitacao tipo);

    @Query("select s from SolicitacaoEntrada s join fetch s.moradia left join fetch s.usuario " +
            "where s.id = :id")
    Optional<SolicitacaoEntrada> findPorId(@Param("id") Long id);

    List<SolicitacaoEntrada> findByMoradiaIdAndTipoAndStatusAndEmailConvidado(Long moradiaId,
                                                                              TipoSolicitacao tipo,
                                                                              StatusSolicitacao status,
                                                                              String emailConvidado);

    boolean existsByMoradiaIdAndTipoAndStatusAndEmailConvidado(Long moradiaId,
                                                               TipoSolicitacao tipo,
                                                               StatusSolicitacao status,
                                                               String emailConvidado);

    boolean existsByMoradiaIdAndUsuarioIdAndTipoAndStatus(Long moradiaId,
                                                          Long usuarioId,
                                                          TipoSolicitacao tipo,
                                                          StatusSolicitacao status);
}