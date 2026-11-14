package fiap.remembered.enums;

/** Ciclo da dose: nasce PENDENTE; TOMADA e PULADA sao finais e nunca voltam a PENDENTE. */
public enum StatusDose {

    PENDENTE, TOMADA, PULADA;

    public boolean podeSerConfirmada() {
        return this == PENDENTE;
    }
}
