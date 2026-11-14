package fiap.remembered.security;

import fiap.remembered.config.JwtProperties;
import fiap.remembered.entity.Usuario;
import fiap.remembered.enums.Perfil;
import fiap.remembered.util.Fuso;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres!!";
    private static final String AUDIENCE = "remembered";

    private JwtService jwtService;

    @BeforeEach
    void preparar() {
        var propriedades = new JwtProperties(SEGREDO, AUDIENCE, "remembered-java",
                List.of("remembered-java", "remembered-dotnet"), Duration.ofHours(24));
        jwtService = new JwtService(propriedades, Clock.system(Fuso.ZONA));
    }

    @Test
    void tokenDoIdosoEmitidoPelaApiEInterpretadoComSeuId() {
        Usuario idoso = Usuario.novo("Maria", "maria@email.com", "hash");
        idoso.setId(42L);

        Optional<UsuarioLogado> logado = jwtService.interpretar(jwtService.emitir(idoso).valor());

        assertThat(logado).hasValueSatisfying(usuario -> {
            assertThat(usuario.perfil()).isEqualTo(Perfil.IDOSO);
            assertThat(usuario.idosoId()).isEqualTo(42L);
            assertThat(usuario.nome()).isEqualTo("Maria");
        });
    }

    @Test
    void tokenDoCuidadorEmitidoPelaApiDotnetTrazOsIdososVinculados() {
        String token = tokenDoCuidador("remembered-dotnet", AUDIENCE, SEGREDO);

        Optional<UsuarioLogado> logado = jwtService.interpretar(token);

        assertThat(logado).hasValueSatisfying(usuario -> {
            assertThat(usuario.perfil()).isEqualTo(Perfil.CUIDADOR);
            assertThat(usuario.enxerga(7L)).isTrue();
            assertThat(usuario.enxerga(8L)).isTrue();
            assertThat(usuario.enxerga(9L)).isFalse();
        });
    }

    @Test
    void rejeitaIssuerQueNaoEstaNaListaDeAceitos() {
        assertThat(jwtService.interpretar(tokenDoCuidador("estranho", AUDIENCE, SEGREDO))).isEmpty();
    }

    @Test
    void rejeitaAudienceDiferente() {
        assertThat(jwtService.interpretar(tokenDoCuidador("remembered-dotnet", "outro-app", SEGREDO))).isEmpty();
    }

    @Test
    void rejeitaTokenAssinadoComOutraChave() {
        String outraChave = "outra-chave-secreta-com-mais-de-32-caracteres!!";
        assertThat(jwtService.interpretar(tokenDoCuidador("remembered-dotnet", AUDIENCE, outraChave))).isEmpty();
    }

    @Test
    void rejeitaTextoQueNaoEUmToken() {
        assertThat(jwtService.interpretar("isto-nao-e-um-jwt")).isEmpty();
    }

    private String tokenDoCuidador(String issuer, String audience, String segredo) {
        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject("c-1")
                .claim("role", "CUIDADOR")
                .claim("nome", "Joana")
                .claim("idosos", List.of(7, 8))
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }
}
