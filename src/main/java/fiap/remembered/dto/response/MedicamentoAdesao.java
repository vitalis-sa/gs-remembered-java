package fiap.remembered.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Numeros de um medicamento no periodo. {@code adesao} e {@code variacaoSemanaAnterior} (em pontos
 * percentuais) vem de pkg_adesao e sao nulos quando nao ha doses encerradas para comparar.
 */
public record MedicamentoAdesao(
        Long medicamentoId,
        String nome,
        long dosesPrevistas,
        long tomadas,
        long puladas,
        BigDecimal adesao,
        List<String> horariosMaisPulados,
        BigDecimal variacaoSemanaAnterior) {
}
