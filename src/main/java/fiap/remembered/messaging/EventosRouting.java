package fiap.remembered.messaging;

import lombok.Getter;

/** Routing keys do exchange topic {@code remembered.eventos}. */
@Getter
public enum EventosRouting {

    DOSE_PULADA("dose.pulada"),
    MEDICAMENTO_CRIADO("medicamento.criado"),
    CONSULTA_CRIADA("consulta.criada");

    private final String chave;

    EventosRouting(String chave) {
        this.chave = chave;
    }
}
