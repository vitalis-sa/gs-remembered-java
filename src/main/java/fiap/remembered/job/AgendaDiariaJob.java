package fiap.remembered.job;

import fiap.remembered.service.GeradorDeDoses;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Todo dia, logo apos a meia-noite, completa a agenda dos proximos 7 dias de cada medicamento. */
@Component
@RequiredArgsConstructor
public class AgendaDiariaJob {

    private final GeradorDeDoses geradorDeDoses;

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Sao_Paulo")
    public void executar() {
        geradorDeDoses.completarAgendaDeTodos();
    }
}
