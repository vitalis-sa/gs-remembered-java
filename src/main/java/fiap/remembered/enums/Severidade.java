package fiap.remembered.enums;

import fiap.remembered.util.Enums;

/** Resultado de pkg_adesao.fn_severidade_alerta. So ALTA e MEDIA geram evento para o cuidador. */
public enum Severidade {

    ALTA, MEDIA, NENHUM;

    public boolean geraEvento() {
        return this != NENHUM;
    }

    /** Valor desconhecido ou nulo vindo do banco e tratado como NENHUM: na duvida, nao alarmar. */
    public static Severidade deTexto(String texto) {
        return Enums.buscar(Severidade.class, texto).orElse(NENHUM);
    }
}
