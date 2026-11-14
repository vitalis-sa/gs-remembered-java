package fiap.remembered.dto.response;

import fiap.remembered.ai.AnaliseAdesaoLlm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** {@code analise} e nula quando o LLM falhou; nesse caso {@code aviso} explica que so ha os numeros. */
public record RelatorioAdesaoResponse(
        Long id,
        Long idosoId,
        LocalDate periodoInicio,
        LocalDate periodoFim,
        BigDecimal percentualAdesao,
        DadosAdesao dados,
        AnaliseAdesaoLlm analise,
        String aviso,
        LocalDateTime geradoEm) {
}
