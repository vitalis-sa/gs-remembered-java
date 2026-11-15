package fiap.remembered.job;

import fiap.remembered.service.DosesVencidasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** A cada 60 s procura doses vencidas. fixedDelay: uma execucao so comeca depois que a anterior terminou. */
@Slf4j
@Component
@ConditionalOnProperty(name = "remembered.jobs.habilitados", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DosesVencidasJob {

    private final DosesVencidasService service;

    @Scheduled(fixedDelayString = "${remembered.doses.intervalo-job-ms:60000}")
    public void executar() {
        int marcadas = service.marcarVencidas();
        log.debug("Doses vencidas marcadas como PULADA: {}", marcadas);
    }
}
