package fiap.remembered.repository;

import fiap.remembered.entity.RegistroDose;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/** Porta de entrada para as funcoes do pacote pkg_adesao: o calculo mora no Oracle, nao no Java. */
public interface AdesaoRepository extends Repository<RegistroDose, Long> {

    @Query(value = "SELECT pkg_adesao.fn_percentual_adesao(:medicamentoId, :inicio, :fim) FROM dual", nativeQuery = true)
    Optional<BigDecimal> percentualAdesao(@Param("medicamentoId") Long medicamentoId,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fim") LocalDateTime fim);

    @Query(value = "SELECT pkg_adesao.fn_severidade_alerta(:doseId) FROM dual", nativeQuery = true)
    String severidadeDoAlerta(@Param("doseId") Long doseId);
}
