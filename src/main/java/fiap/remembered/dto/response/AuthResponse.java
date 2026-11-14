package fiap.remembered.dto.response;

import java.time.LocalDateTime;

public record AuthResponse(String token, String tipo, LocalDateTime expiraEm, UsuarioResponse usuario) {

    public static AuthResponse bearer(String token, LocalDateTime expiraEm, UsuarioResponse usuario) {
        return new AuthResponse(token, "Bearer", expiraEm, usuario);
    }
}
