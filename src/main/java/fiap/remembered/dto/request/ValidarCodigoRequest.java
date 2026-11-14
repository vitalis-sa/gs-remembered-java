package fiap.remembered.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidarCodigoRequest(@NotBlank(message = "Código é obrigatório") String codigo) {
}
