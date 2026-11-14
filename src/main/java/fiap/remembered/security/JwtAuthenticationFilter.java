package fiap.remembered.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Le {@code Authorization: Bearer <token>} e, se o token servir, poe o usuario no contexto da
 * requisicao. Nunca rejeita por conta propria: sem token o contexto fica vazio e quem decide se
 * a rota exige autenticacao e a cadeia de autorizacao. Nao consulta o banco.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(cabecalho -> cabecalho.startsWith(PREFIXO))
                .map(cabecalho -> cabecalho.substring(PREFIXO.length()))
                .flatMap(jwtService::interpretar)
                .ifPresent(this::autenticar);
        filterChain.doFilter(request, response);
    }

    private void autenticar(UsuarioLogado usuario) {
        var autenticacao = new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority(usuario.perfil().authority())));
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }
}
