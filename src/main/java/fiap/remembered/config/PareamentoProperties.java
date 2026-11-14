package fiap.remembered.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("remembered.pareamento")
public record PareamentoProperties(int validadeMinutos) {
}
