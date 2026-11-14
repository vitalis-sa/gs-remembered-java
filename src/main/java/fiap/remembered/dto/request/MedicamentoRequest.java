package fiap.remembered.dto.request;

import fiap.remembered.enums.TipoCriador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record MedicamentoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 60, message = "Dosagem deve ter no máximo 60 caracteres") String dosagem,
        @Size(max = 40, message = "Forma deve ter no máximo 40 caracteres") String forma,
        @Size(max = 20, message = "Cor do ícone deve ter no máximo 20 caracteres") String corIcone,
        @Size(max = 20, message = "Formato do ícone deve ter no máximo 20 caracteres") String formatoIcone,

        @Schema(description = "Crítico: alerta o cuidador na primeira dose pulada. Padrão: false.")
        Boolean critico,

        @Schema(description = "Doses puladas em 7 dias que geram alerta MEDIA. Padrão: 3.")
        @Min(value = 1, message = "Limite de pulos deve ser no mínimo 1")
        Integer limitePulos,

        @NotNull(message = "Data de início é obrigatória") LocalDate dataInicio,
        LocalDate dataFim,

        @Schema(description = "Padrão: true.") Boolean ativo,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres") String observacoes,

        @Schema(description = "Use CHAT quando o cadastro vem do assistente do idoso. Só vale para o idoso.")
        TipoCriador origem,

        @NotEmpty(message = "Informe ao menos um horário")
        List<@Valid HorarioRequest> horarios) {
}
