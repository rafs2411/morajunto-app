package br.com.morajunto.morajunto_api.despesa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByMoradiaIdOrderByDataPagamentoDescIdDesc(Long moradiaId);

    Optional<Pagamento> findByIdAndMoradiaId(Long id, Long moradiaId);
}