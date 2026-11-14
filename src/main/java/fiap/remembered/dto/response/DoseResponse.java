package fiap.remembered.dto.response;

import fiap.remembered.enums.OrigemDose;
import fiap.remembered.enums.StatusDose;

import java.time.LocalDateTime;

public record DoseResponse(
        Long id,
        Long medicamentoId,
        String medicamentoNome,
        LocalDateTime horarioPrevisto,
        StatusDose status,
        LocalDateTime confirmadaEm,
        OrigemDose origem) {
}
