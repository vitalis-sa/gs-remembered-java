package fiap.remembered.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Optional;

public record RelatorioAdesaoRequest(
        @Schema(description = "Quantos dias, até hoje, o relatório cobre. Padrão: 7.", example = "7")
        @Min(value = 1, message = "Período deve ter ao menos 1 dia")
        @Max(value = 90, message = "Período deve ter no máximo 90 dias")
        Integer dias) {

    private static final int DIAS_PADRAO = 7;

    /** Corpo opcional: sem corpo, vale o periodo padrao. */
    public static RelatorioAdesaoRequest padrao() {
        return new RelatorioAdesaoRequest(null);
    }

    public int diasOuPadrao() {
        return Optional.ofNullable(dias).orElse(DIAS_PADRAO);
    }
}
