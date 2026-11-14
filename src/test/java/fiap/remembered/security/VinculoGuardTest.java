package fiap.remembered.security;

import fiap.remembered.enums.Perfil;
import fiap.remembered.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VinculoGuardTest {

    private final VinculoGuard guard = new VinculoGuard();
    private final UsuarioLogado idoso = new UsuarioLogado("5", Perfil.IDOSO, "Maria", Set.of());
    private final UsuarioLogado cuidador = new UsuarioLogado("c-1", Perfil.CUIDADOR, "Joana", Set.of(5L, 6L));

    @Test
    void idosoVeApenasOProprioId() {
        assertThat(guard.pode(autenticacao(idoso), 5L)).isTrue();
        assertThat(guard.pode(autenticacao(idoso), 6L)).isFalse();
    }

    @Test
    void cuidadorVeApenasOsIdososDaClaim() {
        assertThat(guard.pode(autenticacao(cuidador), 6L)).isTrue();
        assertThat(guard.pode(autenticacao(cuidador), 7L)).isFalse();
    }

    @Test
    void recursoDeOutroIdosoRespondeComoSeNaoExistisse() {
        assertThatThrownBy(() -> guard.exigirAcesso(idoso, 6L, "Dose", 10L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Dose 10");
        assertThatCode(() -> guard.exigirAcesso(idoso, 5L, "Dose", 10L)).doesNotThrowAnyException();
    }

    private UsernamePasswordAuthenticationToken autenticacao(UsuarioLogado usuario) {
        return new UsernamePasswordAuthenticationToken(usuario, null);
    }
}
