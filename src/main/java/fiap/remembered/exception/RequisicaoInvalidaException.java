package fiap.remembered.exception;

import org.springframework.http.HttpStatus;

/** 400 para regras de validacao que a Bean Validation nao consegue expressar (ex.: data fim antes do inicio). */
public class RequisicaoInvalidaException extends ApiException {

    public RequisicaoInvalidaException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }
}
