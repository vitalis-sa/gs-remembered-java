package fiap.remembered.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AdesaoTest {

    @Test
    void adesaoEATomadasSobreTomadasMaisPuladas() {
        assertThat(Adesao.percentual(8, 2)).contains(new BigDecimal("80.00"));
        assertThat(Adesao.percentual(1, 2)).contains(new BigDecimal("33.33"));
    }

    @Test
    void semDosesEncerradasNaoHaPercentual() {
        assertThat(Adesao.percentual(0, 0)).isEmpty();
    }
}
