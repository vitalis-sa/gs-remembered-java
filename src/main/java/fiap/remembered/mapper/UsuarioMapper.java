package fiap.remembered.mapper;

import fiap.remembered.dto.response.UsuarioResponse;
import fiap.remembered.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
