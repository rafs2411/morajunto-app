package br.com.morajunto.morajunto_api.moradia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MoradiaRepository extends JpaRepository<Moradia, Long> {

    @Query("select m from Moradia m " +
            "where m.disponivelParaEntrada = true and (m.vagas is null or m.vagas > 0) " +
            "and not exists (select 1 from Morador mo where mo.moradia = m and mo.usuario.id = :usuarioId and mo.ativo = true) " +
            "order by m.nome")
    List<Moradia> findDisponiveisPara(@Param("usuarioId") Long usuarioId);
}