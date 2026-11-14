package fiap.remembered.dto.response;

import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nome, String email, LocalDateTime criadoEm) {
}
