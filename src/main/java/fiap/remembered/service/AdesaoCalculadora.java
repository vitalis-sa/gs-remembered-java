package fiap.remembered.service;

import fiap.remembered.dto.response.DadosAdesao;
import fiap.remembered.dto.response.MedicamentoAdesao;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.repository.AdesaoRepository;
import fiap.remembered.repository.DoseDoPeriodo;
import fiap.remembered.repository.RegistroDoseRepository;
import fiap.remembered.util.Adesao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Monta os numeros do relatorio. As doses do periodo vem numa unica consulta e sao agrupadas por
 * medicamento em uma passada (HashMap), O(n). O percentual de cada medicamento e a variacao contra o
 * periodo anterior vem do Oracle (pkg_adesao.fn_percentual_adesao), que e a fonte da regra.
 */
@Service
@RequiredArgsConstructor
public class AdesaoCalculadora {

    private static final int MAXIMO_DE_HORARIOS_PULADOS = 3;
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final RegistroDoseRepository doses;
    private final AdesaoRepository adesaoRepository;

    public DadosAdesao calcular(Long idosoId, LocalDate inicio, LocalDate fim) {
        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fim.plusDays(1).atStartOfDay();
        LocalDateTime deAnterior = de.minusDays(ChronoUnit.DAYS.between(inicio, fim) + 1);

        Map<Long, List<DoseDoPeriodo>> porMedicamento = doses.doPeriodo(idosoId, de, ate).stream()
                .collect(groupingBy(DoseDoPeriodo::medicamentoId, LinkedHashMap::new, toList()));

        List<MedicamentoAdesao> medicamentos = porMedicamento.values().stream()
                .map(dosesDoMedicamento -> resumir(dosesDoMedicamento, de, ate, deAnterior))
                .toList();

        long tomadas = medicamentos.stream().mapToLong(MedicamentoAdesao::tomadas).sum();
        long puladas = medicamentos.stream().mapToLong(MedicamentoAdesao::puladas).sum();
        return new DadosAdesao(inicio, fim, Adesao.percentual(tomadas, puladas).orElse(null), medicamentos);
    }

    private MedicamentoAdesao resumir(List<DoseDoPeriodo> dosesDoMedicamento,
                                      LocalDateTime de,
                                      LocalDateTime ate,
                                      LocalDateTime deAnterior) {
        DoseDoPeriodo referencia = dosesDoMedicamento.get(0);
        Map<StatusDose, Long> porStatus = dosesDoMedicamento.stream()
                .collect(groupingBy(DoseDoPeriodo::status, () -> new EnumMap<>(StatusDose.class), counting()));

        Optional<BigDecimal> adesaoAtual = adesaoRepository.percentualAdesao(referencia.medicamentoId(), de, ate);
        Optional<BigDecimal> adesaoAnterior = adesaoRepository.percentualAdesao(referencia.medicamentoId(), deAnterior, de);

        return new MedicamentoAdesao(
                referencia.medicamentoId(),
                referencia.medicamentoNome(),
                dosesDoMedicamento.size(),
                porStatus.getOrDefault(StatusDose.TOMADA, 0L),
                porStatus.getOrDefault(StatusDose.PULADA, 0L),
                adesaoAtual.orElse(null),
                horariosMaisPulados(dosesDoMedicamento),
                adesaoAtual.flatMap(atual -> adesaoAnterior.map(atual::subtract)).orElse(null));
    }

    /** Conta puladas por horario (HashMap, O(n)) e ordena so os poucos horarios distintos. */
    private List<String> horariosMaisPulados(List<DoseDoPeriodo> dosesDoMedicamento) {
        return dosesDoMedicamento.stream()
                .filter(dose -> dose.status() == StatusDose.PULADA)
                .collect(groupingBy(dose -> dose.horarioPrevisto().toLocalTime().format(HH_MM), counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(MAXIMO_DE_HORARIOS_PULADOS)
                .map(Map.Entry::getKey)
                .toList();
    }
}
