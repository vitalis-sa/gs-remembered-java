package fiap.remembered.enums;

import java.util.Optional;

/** Quem cadastrou um medicamento. CHAT e o assistente do idoso, que age em nome dele. */
public enum TipoCriador {

    IDOSO, CUIDADOR, CHAT;

    /**
     * O criador e o perfil de quem chama; so o idoso pode declarar que o cadastro veio do assistente.
     * Um cuidador que peca CHAT continua sendo registrado como CUIDADOR.
     */
    public static TipoCriador de(Perfil perfil, TipoCriador solicitado) {
        return Optional.ofNullable(solicitado)
                .filter(tipo -> tipo == CHAT && perfil == Perfil.IDOSO)
                .orElse(perfil.getComoCriador());
    }
}
