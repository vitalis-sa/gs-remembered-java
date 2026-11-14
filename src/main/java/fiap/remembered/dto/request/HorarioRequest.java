package fiap.remembered.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record HorarioRequest(
        @Schema(example = "08:00")
        @NotBlank(message = "Hora é obrigatória")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Hora deve estar no formato HH:mm")
        String hora,

        @Schema(description = "Sete dígitos 0/1, da segunda ao domingo. Padrão: todos os dias.", example = "1111111")
        @Pattern(regexp = "^[01]{7}$", message = "Dias da semana deve ter 7 dígitos, 0 ou 1")
        String diasSemana) {
}
