package fiap.remembered.config;

import fiap.remembered.util.Fuso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Relogio injetavel: os services pedem o "agora" a este bean, o que deixa as regras de tempo testaveis. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(Fuso.ZONA);
    }
}
