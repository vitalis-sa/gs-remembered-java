package fiap.remembered.exception;

import org.springframework.http.HttpStatus;

/** 409: tudo existe e o pedido esta bem formado, mas o estado atual do dominio nao permite a operacao. */
public class RegraDeNegocioException extends ApiException {

    public RegraDeNegocioException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
