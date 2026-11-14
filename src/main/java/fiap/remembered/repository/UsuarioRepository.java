package fiap.remembered.repository;

import fiap.remembered.entity.Usuario;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByCodigoPareamento(String codigo);

    boolean existsByCodigoPareamento(String codigo);

    /**
     * Consome o codigo numa unica instrucao: dois cuidadores usando o mesmo codigo ao mesmo tempo
     * nao passam os dois (so um UPDATE afeta a linha). Retorna 1 se consumiu, 0 se nao.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Usuario u
               set u.codigoPareamento = null, u.codigoExpiraEm = null
             where u.id = :id and u.codigoPareamento = :codigo and u.codigoExpiraEm > :agora
            """)
    int consumirCodigo(@Param("id") Long id, @Param("codigo") String codigo, @Param("agora") LocalDateTime agora);
}
