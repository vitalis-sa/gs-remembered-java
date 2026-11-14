package fiap.remembered.dto.response;

import fiap.remembered.enums.TipoAviso;

import java.time.LocalDateTime;

public record AvisoResponse(
        Long id,
        TipoAviso tipo,
        Long referenciaId,
        String titulo,
        String resumo,
        String criadoPorNome,
        LocalDateTime criadoEm,
        LocalDateTime lidoEm,
        boolean lido) {
}
