package fiap.remembered.security;

import fiap.remembered.exception.RecursoNaoEncontradoException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Guarda de vinculo: o IDOSO acessa so o proprio id e o CUIDADOR so os ids da claim "idosos".
 * Usado por {@code @AcessoAoIdoso} (id na rota) e pelos services (id descoberto ao carregar o recurso).
 */
@Component("vinculoGuard")
public class VinculoGuard {

    /** Para {@code @PreAuthorize}: id na rota; negar vira 403. */
    public boolean pode(Authentication autenticacao, Long idosoId) {
        return autenticacao.getPrincipal() instanceof UsuarioLogado usuario && usuario.enxerga(idosoId);
    }

    /**
     * Para recursos buscados por id (medicamento, dose...): negar vira 404, igual a "nao existe".
     * Um 403 confirmaria que o id existe e deixaria enumerar os recursos de outras pessoas.
     */
    public void exigirAcesso(UsuarioLogado usuario, Long idosoDoRecurso, String recurso, Object id) {
        Optional.of(usuario)
                .filter(logado -> logado.enxerga(idosoDoRecurso))
                .orElseThrow(() -> RecursoNaoEncontradoException.de(recurso, id));
    }
}
