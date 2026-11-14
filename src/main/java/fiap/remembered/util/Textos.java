package fiap.remembered.util;

import java.util.Locale;

public final class Textos {

    private Textos() {
    }

    /** E-mail e identificador unico: cadastro e login precisam normalizar do mesmo jeito. */
    public static String emailNormalizado(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
