package fiap.remembered.security;

import fiap.remembered.config.JwtProperties;
import fiap.remembered.entity.Usuario;
import fiap.remembered.enums.Perfil;
import fiap.remembered.util.Fuso;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Emite o token do idoso e interpreta tambem o do cuidador (emitido pela API .NET).
 * As duas APIs compartilham chave (HS256) e audience; o issuer aceito e uma lista de dois emissores.
 */
@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_PERFIL = "role";
    private static final String CLAIM_NOME = "nome";
    private static final String CLAIM_IDOSOS = "idosos";

    private final JwtProperties propriedades;
    private final Clock clock;
    private final SecretKey chave;

    public JwtService(JwtProperties propriedades, Clock clock) {
        this.propriedades = propriedades;
        this.clock = clock;
        this.chave = Keys.hmacShaKeyFor(propriedades.segredo().getBytes(StandardCharsets.UTF_8));
    }

    public TokenEmitido emitir(Usuario idoso) {
        Instant agora = clock.instant();
        Instant expiracao = agora.plus(propriedades.validade());
        String token = Jwts.builder()
                .issuer(propriedades.issuer())
                .audience().add(propriedades.audience()).and()
                .subject(String.valueOf(idoso.getId()))
                .claim(CLAIM_PERFIL, Perfil.IDOSO.name())
                .claim(CLAIM_NOME, idoso.getNome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
        return new TokenEmitido(token, LocalDateTime.ofInstant(expiracao, Fuso.ZONA));
    }

    /**
     * Vazio para qualquer token que nao sirva: assinatura errada, vencido, audience ou issuer
     * desconhecido, papel inexistente. Quem chama nao precisa (nem deve) distinguir o motivo.
     */
    public Optional<UsuarioLogado> interpretar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .requireAudience(propriedades.audience())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims)
                    .filter(this::emissorAceito)
                    .flatMap(this::paraUsuario);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token recusado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private boolean emissorAceito(Claims claims) {
        return propriedades.issuersAceitos().contains(claims.getIssuer());
    }

    private Optional<UsuarioLogado> paraUsuario(Claims claims) {
        return Perfil.deTexto(claims.get(CLAIM_PERFIL, String.class))
                .map(perfil -> new UsuarioLogado(
                        claims.getSubject(),
                        perfil,
                        Optional.ofNullable(claims.get(CLAIM_NOME, String.class)).orElse(perfil.getRotulo()),
                        idososVinculados(claims)));
    }

    private Set<Long> idososVinculados(Claims claims) {
        return Optional.ofNullable(claims.get(CLAIM_IDOSOS, List.class))
                .map(lista -> ((List<?>) lista).stream()
                        .map(Object::toString)
                        .map(Long::valueOf)
                        .collect(Collectors.toUnmodifiableSet()))
                .orElse(Set.of());
    }
}
