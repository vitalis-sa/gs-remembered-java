package fiap.remembered.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PushTokenRequest(
        @NotBlank(message = "Token de push é obrigatório")
        @Size(max = 200, message = "Token de push deve ter no máximo 200 caracteres")
        String pushToken) {
}
