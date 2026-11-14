package fiap.remembered.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("remembered.push")
public record PushProperties(String url) {
}
