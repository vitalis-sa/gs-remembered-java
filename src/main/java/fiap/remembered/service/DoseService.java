package fiap.remembered.service;

import fiap.remembered.dto.response.DoseResponse;
import fiap.remembered.entity.RegistroDose;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.exception.RecursoNaoEncontradoException;
import fiap.remembered.mapper.DoseMapper;
import fiap.remembered.repository.RegistroDoseRepository;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.security.VinculoGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class DoseService {

    private static final String RECURSO = "Dose";

    private final RegistroDoseRepository doses;
    private final DoseMapper mapper;
    private final VinculoGuard vinculoGuard;
    private final Clock clock;

    /**
     * Doses de um dia (hoje por padrao). O dia de um idoso tem poucas dezenas de doses, entao filtrar
     * por status em memoria custa O(n) sobre poucas linhas e evita uma segunda consulta quase igual.
     */
    @Transactional(readOnly = true)
    public List<DoseResponse> listar(Long idosoId, LocalDate data, StatusDose status) {
        LocalDate dia = Optional.ofNullable(data).orElseGet(() -> LocalDate.now(clock));
        List<RegistroDose> doDia = doses.doIdosoEntre(idosoId, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
        return mapper.toResponses(doDia.stream().filter(porStatus(status)).toList());
    }

    @Transactional
    public DoseResponse confirmar(Long doseId, UsuarioLogado logado) {
        RegistroDose dose = doses.buscarComMedicamento(doseId)
                .orElseThrow(() -> RecursoNaoEncontradoException.de(RECURSO, doseId));
        vinculoGuard.exigirAcesso(logado, dose.getMedicamento().getIdoso().getId(), RECURSO, doseId);
        dose.confirmar(logado.perfil().getComoOrigem(), LocalDateTime.now(clock));
        return mapper.toResponse(dose);
    }

    private static Predicate<RegistroDose> porStatus(StatusDose status) {
        return Optional.ofNullable(status)
                .<Predicate<RegistroDose>>map(desejado -> dose -> dose.getStatus() == desejado)
                .orElse(dose -> true);
    }
}
