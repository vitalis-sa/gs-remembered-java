package fiap.remembered.messaging.evento;

import fiap.remembered.enums.TipoAviso;
import fiap.remembered.enums.TipoCriador;

/**
 * Evento de algo cadastrado para o idoso (medicamento ou consulta). Em ambos o efeito e o mesmo:
 * se quem cadastrou foi o cuidador, o idoso recebe um aviso e um push. Esta interface e o que
 * deixa o consumidor tratar os dois sem duplicar codigo.
 */
public interface NovidadeParaIdoso extends EventoDeDominio {

    Long idosoId();

    TipoCriador criadoPorTipo();

    String criadoPorNome();

    TipoAviso tipoAviso();

    Long referenciaId();

    String titulo();

    String resumo();

    default boolean criadaPorCuidador() {
        return criadoPorTipo() == TipoCriador.CUIDADOR;
    }
}
