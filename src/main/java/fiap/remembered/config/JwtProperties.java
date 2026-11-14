package fiap.remembered.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuracao dos tokens. Segredo e audience sao comuns as duas APIs; esta emite o proprio issuer
 * e aceita a lista de issuers (o da API .NET inclusive).
 */
@ConfigurationProperties("remembered.jwt")
public record JwtProperties(String segredo,
                            String audience,
                            String issuer,
                            List<String> issuersAceitos,
                            Duration validade) {
}
