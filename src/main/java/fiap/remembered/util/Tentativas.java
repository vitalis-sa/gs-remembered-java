package fiap.remembered.util;

import lombok.extern.slf4j.Slf4j;

/** Repete uma acao com espera crescente (backoff exponencial): espera, 2x espera, 4x espera... */
@Slf4j
public final class Tentativas {

    private Tentativas() {
    }

    public static void executar(int maximoDeTentativas, long esperaInicialMs, Runnable acao) {
        for (int tentativa = 1; ; tentativa++) {
            try {
                acao.run();
                return;
            } catch (RuntimeException e) {
                if (tentativa >= maximoDeTentativas) {
                    throw e;
                }
                long espera = esperaInicialMs * (1L << (tentativa - 1));
                log.warn("Tentativa {}/{} falhou ({}); nova tentativa em {} ms", tentativa, maximoDeTentativas, e.getMessage(), espera);
                dormir(espera);
            }
        }
    }

    private static void dormir(long milissegundos) {
        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido durante a espera entre tentativas", e);
        }
    }
}
