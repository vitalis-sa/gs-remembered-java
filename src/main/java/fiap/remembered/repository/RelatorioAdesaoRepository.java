package fiap.remembered.repository;

import fiap.remembered.entity.RelatorioAdesao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RelatorioAdesaoRepository extends BaseRepository<RelatorioAdesao, Long> {

    Page<RelatorioAdesao> findByIdosoId(Long idosoId, Pageable pageable);

    /** Apaga os relatorios de adesao do idoso. Usado na exclusao da conta (LGPD). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RelatorioAdesao r where r.idoso.id = :idosoId")
    int excluirDoIdoso(@Param("idosoId") Long idosoId);
}
