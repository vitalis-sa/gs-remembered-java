package fiap.remembered.exception;

import org.springframework.http.HttpStatus;

/** 422: o codigo nao existe, expirou ou ja foi usado. A mensagem nao diz qual, para nao ajudar tentativa e erro. */
public class CodigoPareamentoInvalidoException extends ApiException {

    public CodigoPareamentoInvalidoException() {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Código de pareamento inválido ou expirado");
    }
}
