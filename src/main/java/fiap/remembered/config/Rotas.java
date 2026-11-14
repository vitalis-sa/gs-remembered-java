package fiap.remembered.config;

/** Mapa unico das rotas: controllers e regras de seguranca partem das mesmas constantes. */
public final class Rotas {

    public static final String BASE = "/api/v1";

    public static final String[] PUBLICAS = {
            BASE + "/auth/**",
            "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**",
            "/actuator/health", "/actuator/health/**",
            "/error"
    };

    /** Rotas da conta do proprio idoso (o cuidador nao tem /me nem avisos nesta API). */
    public static final String[] SOMENTE_IDOSO = {
            BASE + "/me", BASE + "/me/**",
            BASE + "/avisos", BASE + "/avisos/**"
    };

    /** Pareamento e relatorios: quem consome e o cuidador, pela API .NET, com o token dele. */
    public static final String[] SOMENTE_CUIDADOR = {
            BASE + "/pareamento/**",
            BASE + "/idosos/*/relatorios-adesao",
            BASE + "/relatorios-adesao/**"
    };

    private Rotas() {
    }
}
