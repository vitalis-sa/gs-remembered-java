package fiap.remembered.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Traduz toda excecao em ProblemDetail (RFC 9457) com status, title, detail, path e, quando ha,
 * errors[] por campo. Estende ResponseEntityExceptionHandler: as excecoes do proprio Spring MVC
 * (404 de rota, 405, 415, parametro ausente...) ja saem com o status certo e so precisam do path.
 *
 * A seguranca reaproveita este handler: o entry point e o access denied handler delegam a ele.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Todas as excecoes de dominio: o status vem da propria excecao. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> aoViolarRegra(ApiException ex, WebRequest request) {
        return responder(ex.getStatus(), ex.getMessage(), request);
    }

    /**
     * "Senha errada" e "e-mail inexistente" respondem igual: dizer qual falhou
     * permitiria descobrir quais e-mails estao cadastrados.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> aoErrarCredenciais(BadCredentialsException ex, WebRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> aoFaltarAutenticacao(AuthenticationException ex, WebRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "Autenticação necessária: envie um token Bearer válido", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> aoNegarAcesso(AccessDeniedException ex, WebRequest request) {
        return responder(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta operação", request);
    }

    /** Violacoes de {@code @Validated} em parametros de rota/consulta (nao em corpo). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> aoViolarParametro(ConstraintViolationException ex, WebRequest request) {
        List<ErroDeCampo> erros = ex.getConstraintViolations().stream()
                .map(violacao -> new ErroDeCampo(violacao.getPropertyPath().toString(), violacao.getMessage()))
                .toList();
        return responderComErros(HttpStatus.BAD_REQUEST, "Parâmetro inválido", erros, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> aoViolarIntegridade(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "Violação de integridade de dados", request);
    }

    /** Rede de seguranca: nada vaza para o cliente alem de uma mensagem generica; o detalhe fica no log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> aoFalharSemTratamento(Exception ex, WebRequest request) {
        log.error("Erro não tratado ao processar a requisição", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ErroDeCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroDeCampo(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return responderComErros(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição", erros, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formado", request);
    }

    /** Ponto unico de montagem: todo ProblemDetail, inclusive os do Spring, recebe o path aqui. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body,
                                                          HttpHeaders headers,
                                                          HttpStatusCode statusCode,
                                                          WebRequest request) {
        if (body instanceof ProblemDetail problema && request instanceof ServletWebRequest web) {
            problema.setProperty("path", web.getRequest().getRequestURI());
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ResponseEntity<Object> responder(HttpStatusCode status, String detalhe, WebRequest request) {
        return createResponseEntity(ProblemDetail.forStatusAndDetail(status, detalhe), new HttpHeaders(), status, request);
    }

    private ResponseEntity<Object> responderComErros(HttpStatusCode status,
                                                     String detalhe,
                                                     List<ErroDeCampo> erros,
                                                     WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setProperty("errors", erros);
        return createResponseEntity(problema, new HttpHeaders(), status, request);
    }
}
