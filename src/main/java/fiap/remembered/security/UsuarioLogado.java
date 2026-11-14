package fiap.remembered.security;

import fiap.remembered.enums.Perfil;

import java.util.Set;

/**
 * Quem esta chamando, extraido do token sem consultar o banco (a API e stateless).
 *
 * @param subject id do idoso (token desta API) ou do cuidador (token da API .NET)
 * @param idosos  ids dos idosos vinculados ao cuidador; vazio para o idoso. Set: a pergunta
 *                "este id esta vinculado?" e feita a cada requisicao e custa O(1).
 */
public record UsuarioLogado(String subject, Perfil perfil, String nome, Set<Long> idosos) {

    /** So faz sentido para o idoso, cujo subject e o proprio id. As rotas que o usam sao so do idoso. */
    public Long idosoId() {
        return Long.valueOf(subject);
    }

    public boolean enxerga(Long idosoId) {
        return perfil == Perfil.IDOSO ? idosoId().equals(idosoId) : idosos.contains(idosoId);
    }
}
