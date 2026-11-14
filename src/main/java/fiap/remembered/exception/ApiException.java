package fiap.remembered.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base das excecoes de dominio. Cada subclasse fixa o seu status HTTP, e o GlobalExceptionHandler
 * traduz todas com um unico metodo: criar uma regra nova nao exige mexer no handler.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
}
