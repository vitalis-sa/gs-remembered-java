package fiap.remembered.service;

import fiap.remembered.config.DosesProperties;
import fiap.remembered.entity.Medicamento;
import fiap.remembered.entity.RegistroDose;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.repository.MedicamentoRepository;
import fiap.remembered.repository.RegistroDoseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Mantem a agenda de doses dos proximos dias, a partir dos horarios de cada medicamento. */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeradorDeDoses {

    private final RegistroDoseRepository doses;
    private final MedicamentoRepository medicamentos;
    private final DosesProperties propriedades;
    private final Clock clock;

    /**
     * Gera as doses que faltam. Idempotente: um HashSet com os horarios ja gravados faz cada
     * candidata custar O(1), e o total fica O(dias x horarios) em vez de uma consulta por dose.
     * Horarios que ja passaram hoje nao viram dose: nasceriam PENDENTES e seriam marcadas como
     * PULADAS (e alertariam o cuidador) por algo que a pessoa nunca teve chance de tomar.
     */
    @Transactional
    public void agendar(Medicamento medicamento) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock);
        Set<LocalDateTime> jaGerados = new HashSet<>(doses.horariosGeradosAPartirDe(medicamento.getId(), hoje.atStartOfDay()));

        List<RegistroDose> novas = hoje.datesUntil(hoje.plusDays(propriedades.diasAFrente()))
                .filter(medicamento::vigenteEm)
                .flatMap(dia -> horariosDoDia(medicamento, dia))
                .filter(horario -> horario.isAfter(agora))
                .filter(horario -> !jaGerados.contains(horario))
                .map(horario -> RegistroDose.pendente(medicamento, horario))
                .toList();
        doses.saveAll(novas);
    }

    /** Apos editar um medicamento, descarta as doses futuras ainda pendentes e gera de novo. */
    @Transactional
    public void reagendar(Medicamento medicamento) {
        doses.removerDoMedicamento(medicamento.getId(), StatusDose.PENDENTE, LocalDateTime.now(clock));
        agendar(medicamento);
    }

    /** Rotina diaria: o "7 dias a frente" precisa andar junto com o calendario. */
    public void completarAgendaDeTodos() {
        List<Medicamento> vigentes = medicamentos.vigentes(LocalDate.now(clock));
        vigentes.forEach(this::agendarSemInterromper);
        log.info("Agenda de doses completada para {} medicamento(s)", vigentes.size());
    }

    private void agendarSemInterromper(Medicamento medicamento) {
        try {
            agendar(medicamento);
        } catch (RuntimeException e) {
            log.error("Falha ao completar a agenda do medicamento {}: {}", medicamento.getId(), e.getMessage());
        }
    }

    private Stream<LocalDateTime> horariosDoDia(Medicamento medicamento, LocalDate dia) {
        return medicamento.getHorarios().stream()
                .filter(horario -> horario.ocorreEm(dia.getDayOfWeek()))
                .map(horario -> horario.em(dia));
    }
}
