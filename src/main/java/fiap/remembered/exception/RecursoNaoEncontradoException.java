package fiap.remembered.exception;

import org.springframework.http.HttpStatus;

/**
 * 404. Tambem e a resposta para um recurso que existe mas pertence a outro idoso: um 403 confirmaria
 * a existencia do id e permitiria enumerar o banco.
 */
public class RecursoNaoEncontradoException extends ApiException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }

    public static RecursoNaoEncontradoException de(String recurso, Object id) {
        return new RecursoNaoEncontradoException("%s %s não encontrado(a)".formatted(recurso, id));
    }
}
