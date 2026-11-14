package fiap.remembered.util;

import java.util.Arrays;
import java.util.Optional;

public final class Enums {

    private Enums() {
    }

    /** Busca tolerante (espacos e caixa) que devolve vazio em vez de lancar IllegalArgumentException. */
    public static <E extends Enum<E>> Optional<E> buscar(Class<E> tipo, String texto) {
        return Optional.ofNullable(texto)
                .map(String::trim)
                .map(String::toUpperCase)
                .flatMap(nome -> Arrays.stream(tipo.getEnumConstants())
                        .filter(constante -> constante.name().equals(nome))
                        .findFirst());
    }
}
