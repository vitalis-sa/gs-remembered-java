package fiap.remembered.repository;

import fiap.remembered.entity.RegistroDose;
import fiap.remembered.enums.StatusDose;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroDoseRepository extends BaseRepository<RegistroDose, Long> {

    @Query("""
            select d from RegistroDose d join fetch d.medicamento m
             where m.idoso.id = :idosoId and d.horarioPrevisto >= :inicio and d.horarioPrevisto < :fim
             order by d.horarioPrevisto
            """)
    List<RegistroDose> doIdosoEntre(@Param("idosoId") Long idosoId,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fim") LocalDateTime fim);

    @Query("select d from RegistroDose d join fetch d.medicamento m where d.id = :id")
    Optional<RegistroDose> buscarComMedicamento(@Param("id") Long id);

    /** Horarios ja gerados a partir de um instante: base para a geracao da agenda nao duplicar doses. */
    @Query("""
            select d.horarioPrevisto from RegistroDose d
             where d.medicamento.id = :medicamentoId and d.horarioPrevisto >= :inicio
            """)
    List<LocalDateTime> horariosGeradosAPartirDe(@Param("medicamentoId") Long medicamentoId,
                                                 @Param("inicio") LocalDateTime inicio);

    /** Usa idx_dose_status_horario. O join fetch evita 2 consultas por dose ao montar o evento. */
    @Query("""
            select d from RegistroDose d join fetch d.medicamento m join fetch m.idoso
             where d.status = :status and d.horarioPrevisto < :limite
             order by d.horarioPrevisto
            """)
    List<RegistroDose> comStatusAntesDe(@Param("status") StatusDose status, @Param("limite") LocalDateTime limite);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from RegistroDose d
             where d.medicamento.id = :medicamentoId and d.status = :status and d.horarioPrevisto >= :aPartirDe
            """)
    int removerDoMedicamento(@Param("medicamentoId") Long medicamentoId,
                             @Param("status") StatusDose status,
                             @Param("aPartirDe") LocalDateTime aPartirDe);

    @Query("""
            select new fiap.remembered.repository.DoseDoPeriodo(m.id, m.nome, d.horarioPrevisto, d.status)
              from RegistroDose d join d.medicamento m
             where m.idoso.id = :idosoId and d.horarioPrevisto >= :inicio and d.horarioPrevisto < :fim
            """)
    List<DoseDoPeriodo> doPeriodo(@Param("idosoId") Long idosoId,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim);
}
