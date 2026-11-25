package br.com.morajunto.morajunto_api.compra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemCompraRepository extends JpaRepository<ItemCompra, Long> {

    @Query("select i from ItemCompra i " +
            "left join fetch i.compradoPor left join fetch i.criadoPor " +
            "where i.moradia.id = :moradiaId and i.comprado = :comprado " +
            "order by i.criadoEm desc, i.id desc")
    List<ItemCompra> findDaMoradia(@Param("moradiaId") Long moradiaId, @Param("comprado") boolean comprado);

    @Query("select i from ItemCompra i " +
            "left join fetch i.compradoPor left join fetch i.criadoPor " +
            "where i.id = :id and i.moradia.id = :moradiaId")
    Optional<ItemCompra> findPorId(@Param("id") Long id, @Param("moradiaId") Long moradiaId);
}