package br.com.morajunto.morajunto_api.tarefa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TarefaOcorrenciaRepository extends JpaRepository<TarefaOcorrencia, Long> {

    Optional<TarefaOcorrencia> findByTarefaIdAndDataOcorrencia(Long tarefaId, LocalDate dataOcorrencia);

    @Query("select o from TarefaOcorrencia o " +
            "where o.tarefa.id in :tarefaIds and o.dataOcorrencia between :inicio and :fim")
    List<TarefaOcorrencia> findDoPeriodo(@Param("tarefaIds") Collection<Long> tarefaIds,
                                         @Param("inicio") LocalDate inicio,
                                         @Param("fim") LocalDate fim);
}