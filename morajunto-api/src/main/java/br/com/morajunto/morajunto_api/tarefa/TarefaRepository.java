package br.com.morajunto.morajunto_api.tarefa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @Query("select t from Tarefa t join fetch t.responsavel " +
            "where t.moradia.id = :moradiaId and t.ativa = true order by t.nome")
    List<Tarefa> findAtivasDaMoradia(@Param("moradiaId") Long moradiaId);

    @Query("select t from Tarefa t join fetch t.responsavel " +
            "where t.id = :id and t.moradia.id = :moradiaId and t.ativa = true")
    Optional<Tarefa> findAtivaPorId(@Param("id") Long id, @Param("moradiaId") Long moradiaId);

    @Query("select t from Tarefa t join fetch t.responsavel " +
            "where t.moradia.id = :moradiaId and t.ativa = true " +
            "and t.dataInicio <= :fim and (t.dataFim is null or t.dataFim >= :inicio) " +
            "order by t.horario, t.nome")
    List<Tarefa> findAtivasNoPeriodo(@Param("moradiaId") Long moradiaId,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);
}