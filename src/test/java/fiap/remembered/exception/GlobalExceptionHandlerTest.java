package fiap.remembered.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final ServletWebRequest requisicao = new ServletWebRequest(new MockHttpServletRequest("GET", "/api/v1/doses/9"));

    @Test
    void cadaExcecaoDeDominioSaiComOStatusDefinidoPorElaMesma() {
        assertThat(status(handler.aoViolarRegra(new RecursoNaoEncontradoException("sumiu"), requisicao))).isEqualTo(404);
        assertThat(status(handler.aoViolarRegra(new EmailJaCadastradoException(), requisicao))).isEqualTo(409);
        assertThat(status(handler.aoViolarRegra(new RegraDeNegocioException("ja tomada"), requisicao))).isEqualTo(409);
        assertThat(status(handler.aoViolarRegra(new RequisicaoInvalidaException("datas"), requisicao))).isEqualTo(400);
        assertThat(status(handler.aoViolarRegra(new CodigoPareamentoInvalidoException(), requisicao))).isEqualTo(422);
    }

    @Test
    void problemaTrazDetalheEPath() {
        ResponseEntity<Object> resposta = handler.aoViolarRegra(RecursoNaoEncontradoException.de("Dose", 9L), requisicao);

        ProblemDetail problema = (ProblemDetail) resposta.getBody();
        assertThat(problema).isNotNull();
        assertThat(problema.getDetail()).isEqualTo("Dose 9 não encontrado(a)");
        assertThat(problema.getProperties()).containsEntry("path", "/api/v1/doses/9");
    }

    @Test
    void falhasDeSegurancaSaemComo401E403() {
        assertThat(status(handler.aoErrarCredenciais(new BadCredentialsException("x"), requisicao))).isEqualTo(401);
        assertThat(status(handler.aoFaltarAutenticacao(new InsufficientAuthenticationException("x"), requisicao))).isEqualTo(401);
        assertThat(status(handler.aoNegarAcesso(new AccessDeniedException("x"), requisicao))).isEqualTo(403);
    }

    @Test
    void credenciaisInvalidasNaoRevelamQualCampoErrou() {
        ProblemDetail problema = (ProblemDetail) handler.aoErrarCredenciais(new BadCredentialsException("senha errada"), requisicao).getBody();

        assertThat(problema).isNotNull();
        assertThat(problema.getDetail()).isEqualTo("Credenciais inválidas");
    }

    @Test
    void violacaoDeIntegridadeVira409EErroInesperadoVira500SemVazarDetalhe() {
        assertThat(status(handler.aoViolarIntegridade(new DataIntegrityViolationException("uk_x"), requisicao))).isEqualTo(409);

        ResponseEntity<Object> resposta = handler.aoFalharSemTratamento(new IllegalStateException("segredo interno"), requisicao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(((ProblemDetail) resposta.getBody()).getDetail()).isEqualTo("Erro interno do servidor");
    }

    private int status(ResponseEntity<Object> resposta) {
        return resposta.getStatusCode().value();
    }
}
