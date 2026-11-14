package fiap.remembered.enums;

import fiap.remembered.util.Enums;
import lombok.Getter;

import java.util.Optional;

/**
 * Papeis da API. Cada papel ja carrega o que ele significa em outros pontos do dominio (quem e o
 * "criador" de um cadastro e de onde vem a confirmacao de uma dose), para nenhum service precisar
 * repetir esse mapeamento.
 */
@Getter
public enum Perfil {

    IDOSO(TipoCriador.IDOSO, OrigemDose.APP, "Idoso"),
    CUIDADOR(TipoCriador.CUIDADOR, OrigemDose.CUIDADOR, "Cuidador");

    private final TipoCriador comoCriador;
    private final OrigemDose comoOrigem;
    /** Nome para exibir quando o token nao traz o nome da pessoa. */
    private final String rotulo;

    Perfil(TipoCriador comoCriador, OrigemDose comoOrigem, String rotulo) {
        this.comoCriador = comoCriador;
        this.comoOrigem = comoOrigem;
        this.rotulo = rotulo;
    }

    /** Nome da authority como o Spring Security a espera ({@code hasRole} assume o prefixo ROLE_). */
    public String authority() {
        return "ROLE_" + name();
    }

    public static Optional<Perfil> deTexto(String texto) {
        return Enums.buscar(Perfil.class, texto);
    }
}
