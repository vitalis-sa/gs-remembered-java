package fiap.remembered.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TentativasTest {

    @Test
    void repeteAteDarCerto() {
        AtomicInteger chamadas = new AtomicInteger();

        Tentativas.executar(3, 1, () -> {
            if (chamadas.incrementAndGet() < 3) {
                throw new IllegalStateException("falha " + chamadas.get());
            }
        });

        assertThat(chamadas).hasValue(3);
    }

    @Test
    void depoisDoLimiteRelancaAUltimaFalha() {
        AtomicInteger chamadas = new AtomicInteger();

        assertThatThrownBy(() -> Tentativas.executar(3, 1, () -> {
            chamadas.incrementAndGet();
            throw new IllegalStateException("sempre falha");
        })).isInstanceOf(IllegalStateException.class).hasMessage("sempre falha");

        assertThat(chamadas).hasValue(3);
    }
}
