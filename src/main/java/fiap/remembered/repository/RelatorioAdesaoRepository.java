package fiap.remembered.repository;

import fiap.remembered.entity.RelatorioAdesao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelatorioAdesaoRepository extends BaseRepository<RelatorioAdesao, Long> {

    Page<RelatorioAdesao> findByIdosoId(Long idosoId, Pageable pageable);
}
