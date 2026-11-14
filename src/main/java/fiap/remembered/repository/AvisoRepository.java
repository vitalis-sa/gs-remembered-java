package fiap.remembered.repository;

import fiap.remembered.entity.Aviso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AvisoRepository extends BaseRepository<Aviso, Long> {

    Page<Aviso> findByIdosoId(Long idosoId, Pageable pageable);

    Page<Aviso> findByIdosoIdAndLidoEmIsNull(Long idosoId, Pageable pageable);

    Page<Aviso> findByIdosoIdAndLidoEmIsNotNull(Long idosoId, Pageable pageable);

    /** O filtro pelo dono faz parte da consulta: aviso de outro idoso simplesmente nao e encontrado. */
    Optional<Aviso> findByIdAndIdosoId(Long id, Long idosoId);

    boolean existsByEventoId(String eventoId);
}
