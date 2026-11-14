package fiap.remembered.repository;

import fiap.remembered.enums.StatusDose;

import java.time.LocalDateTime;

/** Projecao enxuta para o relatorio: so as colunas que o calculo usa, sem carregar entidades. */
public record DoseDoPeriodo(Long medicamentoId, String medicamentoNome, LocalDateTime horarioPrevisto, StatusDose status) {
}
