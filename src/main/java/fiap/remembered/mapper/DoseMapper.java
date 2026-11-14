package fiap.remembered.mapper;

import fiap.remembered.dto.response.DoseResponse;
import fiap.remembered.entity.RegistroDose;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DoseMapper {

    @Mapping(target = "medicamentoId", source = "medicamento.id")
    @Mapping(target = "medicamentoNome", source = "medicamento.nome")
    DoseResponse toResponse(RegistroDose dose);

    List<DoseResponse> toResponses(List<RegistroDose> doses);
}
