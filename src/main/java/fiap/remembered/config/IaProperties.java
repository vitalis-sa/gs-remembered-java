package fiap.remembered.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("remembered.ia")
public record IaProperties(Duration timeout) {
}
