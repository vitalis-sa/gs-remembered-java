package fiap.remembered.messaging.evento;

import fiap.remembered.entity.RegistroDose;
import fiap.remembered.enums.Severidade;
import fiap.remembered.messaging.EventosRouting;

import java.time.LocalDateTime;
import java.util.UUID;

/** Publicado pelo job de doses vencidas; a API .NET cria um alerta por cuidador vinculado. */
public record DosePulada(String eventId,
                         Long doseId,
                         Long idosoId,
                         String idosoNome,
                         String medicamentoNome,
                         Severidade severidade,
                         LocalDateTime horarioPrevisto,
                         LocalDateTime ocorridoEm) implements EventoDeDominio {

    public static DosePulada de(RegistroDose dose, Severidade severidade, LocalDateTime ocorridoEm) {
        return new DosePulada(
                UUID.randomUUID().toString(),
                dose.getId(),
                dose.getMedicamento().getIdoso().getId(),
                dose.getMedicamento().getIdoso().getNome(),
                dose.getMedicamento().getNome(),
                severidade,
                dose.getHorarioPrevisto(),
                ocorridoEm);
    }

    @Override
    public EventosRouting rota() {
        return EventosRouting.DOSE_PULADA;
    }
}
