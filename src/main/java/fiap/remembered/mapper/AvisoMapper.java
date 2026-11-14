package fiap.remembered.mapper;

import fiap.remembered.dto.response.AvisoResponse;
import fiap.remembered.entity.Aviso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvisoMapper {

    @Mapping(target = "lido", expression = "java(aviso.getLidoEm() != null)")
    AvisoResponse toResponse(Aviso aviso);
}
