package fiap.remembered.dto.response;

import fiap.remembered.enums.TipoCriador;

import java.time.LocalDate;
import java.util.List;

public record MedicamentoResponse(
        Long id,
        Long idosoId,
        String nome,
        String dosagem,
        String forma,
        String corIcone,
        String formatoIcone,
        boolean critico,
        int limitePulos,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativo,
        String observacoes,
        TipoCriador criadoPorTipo,
        String criadoPorNome,
        List<HorarioResponse> horarios) {
}
