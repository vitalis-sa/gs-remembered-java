package fiap.remembered.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public final class Adesao {

    private Adesao() {
    }

    /**
     * Adesao = TOMADA / (TOMADA + PULADA), em %. Doses PENDENTES nao entram. Sem doses encerradas
     * nao ha percentual (vazio), e nao "0%": a pessoa nao falhou, apenas nao ha dado.
     */
    public static Optional<BigDecimal> percentual(long tomadas, long puladas) {
        return Optional.of(tomadas + puladas)
                .filter(total -> total > 0)
                .map(total -> BigDecimal.valueOf(tomadas * 100)
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
    }
}
