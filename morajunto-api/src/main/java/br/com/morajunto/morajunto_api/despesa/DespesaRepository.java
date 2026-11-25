package br.com.morajunto.morajunto_api.despesa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    @Query("select d from Despesa d where d.moradia.id = :moradiaId and d.dataDespesa between :inicio and :fim order by d.dataDespesa desc, d.id desc")
    List<Despesa> findDoPeriodo(@Param("moradiaId") Long moradiaId,
                                @Param("inicio") LocalDate inicio,
                                @Param("fim") LocalDate fim);

    Optional<Despesa> findByIdAndMoradiaId(Long id, Long moradiaId);
}