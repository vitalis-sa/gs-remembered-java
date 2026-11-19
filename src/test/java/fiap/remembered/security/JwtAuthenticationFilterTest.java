package fiap.remembered.security;

import fiap.remembered.config.JwtProperties;
import fiap.remembered.entity.Usuario;
import fiap.remembered.enums.Perfil;
import fiap.remembered.util.Fuso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O filtro nunca rejeita por conta propria: poe o usuario no contexto quando o token serve e sempre
 * deixa a requisicao seguir. Quem responde 401 ou 403 e a cadeia de autorizacao.
 */
class JwtAuthenticationFilterTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres!!";

    private JwtService jwtService;
    private JwtAuthenticationFilter filtro;
    private MockHttpServletRequest requisicao;
    private MockHttpServletResponse resposta;
    private MockFilterChain cadeia;

    @BeforeEach
    void preparar() {
        var propriedades = new JwtProperties(SEGREDO, "remembered", "remembered-java",
                List.of("remembered-java"), Duration.ofHours(24));
        jwtService = new JwtService(propriedades, Clock.system(Fuso.ZONA));
        filtro = new JwtAuthenticationFilter(jwtService);
        requisicao = new MockHttpServletRequest("GET", "/api/v1/me");
        resposta = new MockHttpServletResponse();
        cadeia = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private String tokenDoIdoso() {
        Usuario idoso = Usuario.novo("Maria", "maria@teste.dev", "hash");
        idoso.setId(42L);
        return jwtService.emitir(idoso).valor();
    }

    @Test
    void tokenValidoPoeOIdosoNoContextoComOPapelCerto() throws Exception {
        requisicao.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDoIdoso());

        filtro.doFilter(requisicao, resposta, cadeia);

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacao).isNotNull();
        assertThat(autenticacao.getPrincipal()).isInstanceOfSatisfying(UsuarioLogado.class, usuario -> {
            assertThat(usuario.perfil()).isEqualTo(Perfil.IDOSO);
            assertThat(usuario.idosoId()).isEqualTo(42L);
        });
        assertThat(autenticacao.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_IDOSO");
        assertThat(cadeia.getRequest()).isNotNull();
    }

    @Test
    void semCabecalhoOContextoFicaVazioEARequisicaoSegue() throws Exception {
        filtro.doFilter(requisicao, resposta, cadeia);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(cadeia.getRequest()).isNotNull();
        assertThat(resposta.getStatus()).isEqualTo(200);
    }

    @Test
    void tokenInvalidoNaoAutenticaENaoRejeitaPorContaPropria() throws Exception {
        requisicao.addHeader(HttpHeaders.AUTHORIZATION, "Bearer isto-nao-e-um-jwt");

        filtro.doFilter(requisicao, resposta, cadeia);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(cadeia.getRequest()).isNotNull();
        assertThat(resposta.getStatus()).isEqualTo(200);
    }

    @Test
    void cabecalhoSemOPrefixoBearerEIgnorado() throws Exception {
        requisicao.addHeader(HttpHeaders.AUTHORIZATION, tokenDoIdoso());

        filtro.doFilter(requisicao, resposta, cadeia);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(cadeia.getRequest()).isNotNull();
    }
}
