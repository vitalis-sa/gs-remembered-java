package fiap.remembered.security;

import java.time.LocalDateTime;

public record TokenEmitido(String valor, LocalDateTime expiraEm) {
}
