package fiap.remembered.repository;

import fiap.remembered.entity.Aviso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AvisoRepository extends BaseRepository<Aviso, Long> {

    Page<Aviso> findByIdosoId(Long idosoId, Pageable pageable);

    Page<Aviso> findByIdosoIdAndLidoEmIsNull(Long idosoId, Pageable pageable);

    Page<Aviso> findByIdosoIdAndLidoEmIsNotNull(Long idosoId, Pageable pageable);

    /** O filtro pelo dono faz parte da consulta: aviso de outro idoso simplesmente nao e encontrado. */
    Optional<Aviso> findByIdAndIdosoId(Long id, Long idosoId);

    boolean existsByEventoId(String eventoId);

    /** Apaga os avisos do idoso. Usado na exclusao da conta (LGPD). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Aviso a where a.idoso.id = :idosoId")
    int excluirDoIdoso(@Param("idosoId") Long idosoId);
}
