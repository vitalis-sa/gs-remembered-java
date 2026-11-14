package fiap.remembered.service;

import fiap.remembered.config.DosesProperties;
import fiap.remembered.entity.RegistroDose;
import fiap.remembered.enums.Severidade;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.messaging.evento.DosePulada;
import fiap.remembered.repository.AdesaoRepository;
import fiap.remembered.repository.RegistroDoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Doses PENDENTES que passaram da tolerancia (30 min) viram PULADAS. Para cada uma, o Oracle calcula
 * a severidade; ALTA e MEDIA viram evento {@code dose.pulada}, publicado so apos o commit.
 */
@Service
@RequiredArgsConstructor
public class DosesVencidasService {

    private final RegistroDoseRepository doses;
    private final AdesaoRepository adesaoRepository;
    private final DosesProperties propriedades;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    @Transactional
    public int marcarVencidas() {
        LocalDateTime agora = LocalDateTime.now(clock);
        List<RegistroDose> vencidas = doses.comStatusAntesDe(
                StatusDose.PENDENTE, agora.minusMinutes(propriedades.toleranciaMinutos()));

        vencidas.forEach(RegistroDose::marcarComoPulada);
        // A severidade e calculada no banco, que so enxerga as puladas depois do flush.
        doses.flush();
        vencidas.forEach(dose -> publicarSeGrave(dose, agora));
        return vencidas.size();
    }

    private void publicarSeGrave(RegistroDose dose, LocalDateTime agora) {
        Optional.of(Severidade.deTexto(adesaoRepository.severidadeDoAlerta(dose.getId())))
                .filter(Severidade::geraEvento)
                .map(severidade -> DosePulada.de(dose, severidade, agora))
                .ifPresent(eventos::publishEvent);
    }
}
