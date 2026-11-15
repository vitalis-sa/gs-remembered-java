package fiap.remembered.repository;

import fiap.remembered.entity.Medicamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicamentoRepository extends BaseRepository<Medicamento, Long> {

    Page<Medicamento> findByIdosoId(Long idosoId, Pageable pageable);

    /** Medicamentos que ainda geram doses; usado pelo job diario que completa a agenda dos proximos dias. */
    @EntityGraph(attributePaths = "horarios")
    @Query("""
            select m from Medicamento m
             where m.ativo = true and (m.dataFim is null or m.dataFim >= :hoje)
            """)
    List<Medicamento> vigentes(@Param("hoje") LocalDate hoje);

    /** Apaga os medicamentos do idoso; horarios e doses saem pelo ON DELETE CASCADE do banco. Usado na exclusao da conta (LGPD). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Medicamento m where m.idoso.id = :idosoId")
    int excluirDoIdoso(@Param("idosoId") Long idosoId);
}
