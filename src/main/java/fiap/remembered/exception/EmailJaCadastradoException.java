package fiap.remembered.exception;

import org.springframework.http.HttpStatus;

public class EmailJaCadastradoException extends ApiException {

    public EmailJaCadastradoException() {
        super(HttpStatus.CONFLICT, "E-mail já cadastrado");
    }
}
