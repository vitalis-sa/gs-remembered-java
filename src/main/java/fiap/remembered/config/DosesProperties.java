package fiap.remembered.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("remembered.doses")
public record DosesProperties(int diasAFrente, int toleranciaMinutos) {
}
