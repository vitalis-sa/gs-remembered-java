package fiap.remembered.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Codigos curtos para digitar a mao: sem 0/O e 1/I/L, que se confundem na tela. */
public final class CodigoAleatorio {

    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SORTEADOR = new SecureRandom();

    private CodigoAleatorio() {
    }

    public static String gerar(int tamanho) {
        return IntStream.range(0, tamanho)
                .mapToObj(i -> String.valueOf(ALFABETO.charAt(SORTEADOR.nextInt(ALFABETO.length()))))
                .collect(Collectors.joining());
    }
}
