package fiap.remembered.repository;

import fiap.remembered.exception.RecursoNaoEncontradoException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/** Elimina o {@code findById(...).orElseThrow(...)} repetido em todo service. */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    default T obter(ID id, String recurso) {
        return findById(id).orElseThrow(() -> RecursoNaoEncontradoException.de(recurso, id));
    }
}
