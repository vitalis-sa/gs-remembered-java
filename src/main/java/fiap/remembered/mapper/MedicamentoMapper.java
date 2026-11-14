package fiap.remembered.mapper;

import fiap.remembered.dto.request.HorarioRequest;
import fiap.remembered.dto.request.MedicamentoRequest;
import fiap.remembered.dto.response.HorarioResponse;
import fiap.remembered.dto.response.MedicamentoResponse;
import fiap.remembered.entity.HorarioMedicamento;
import fiap.remembered.entity.Medicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicamentoMapper {

    @Mapping(target = "idosoId", source = "idoso.id")
    MedicamentoResponse toResponse(Medicamento medicamento);

    @Mapping(target = "hora", source = "hora", dateFormat = "HH:mm")
    HorarioResponse toResponse(HorarioMedicamento horario);

    /** Dono, criador e horarios nao vem do corpo: o service os define a partir de quem chama. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idoso", ignore = true)
    @Mapping(target = "criadoPorTipo", ignore = true)
    @Mapping(target = "criadoPorNome", ignore = true)
    @Mapping(target = "horarios", ignore = true)
    Medicamento toEntity(MedicamentoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idoso", ignore = true)
    @Mapping(target = "criadoPorTipo", ignore = true)
    @Mapping(target = "criadoPorNome", ignore = true)
    @Mapping(target = "horarios", ignore = true)
    void atualizar(MedicamentoRequest request, @MappingTarget Medicamento medicamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicamento", ignore = true)
    @Mapping(target = "hora", source = "hora", dateFormat = "HH:mm")
    @Mapping(target = "diasSemana", source = "diasSemana", defaultValue = "1111111")
    HorarioMedicamento toEntity(HorarioRequest request);

    List<HorarioMedicamento> toHorarios(List<HorarioRequest> requests);
}
