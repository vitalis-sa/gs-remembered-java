package fiap.remembered.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Tudo o que o relatorio afirma: calculado no Oracle e enviado ao LLM, que so escreve sobre estes numeros. */
public record DadosAdesao(
        LocalDate periodoInicio,
        LocalDate periodoFim,
        BigDecimal percentualGeral,
        List<MedicamentoAdesao> medicamentos) {
}
