package br.com.morajunto.morajunto_api.morador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MoradorRepository extends JpaRepository<Morador, Long> {

    @Query("select m from Morador m join fetch m.moradia where m.usuario.id = :usuarioId and m.ativo = true")
    List<Morador> findAtivosDoUsuario(@Param("usuarioId") Long usuarioId);

    long countByMoradiaIdAndAtivoTrue(Long moradiaId);

    boolean existsByMoradiaIdAndUsuarioIdAndAtivoTrue(Long moradiaId, Long usuarioId);

    List<Morador> findByMoradiaIdAndAtivoTrueOrderByNomeAsc(Long moradiaId);

    Optional<Morador> findByIdAndMoradiaIdAndAtivoTrue(Long id, Long moradiaId);

    Optional<Morador> findByIdAndMoradiaId(Long id, Long moradiaId);

    boolean existsByMoradiaIdAndCor(Long moradiaId, String cor);

    boolean existsByMoradiaIdAndCorAndIdNot(Long moradiaId, String cor, Long id);

    Optional<Morador> findByMoradiaIdAndUsuarioId(Long moradiaId, Long usuarioId);

    Optional<Morador> findFirstByMoradiaIdAndEmailAndUsuarioIsNullAndAtivoTrue(Long moradiaId, String email);
}