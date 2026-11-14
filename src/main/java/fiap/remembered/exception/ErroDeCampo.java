package fiap.remembered.exception;

/** Um item de {@code errors[]} na resposta de validacao. */
public record ErroDeCampo(String campo, String mensagem) {
}
