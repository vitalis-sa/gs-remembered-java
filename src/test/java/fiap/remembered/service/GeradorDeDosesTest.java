package fiap.remembered.service;

import fiap.remembered.config.DosesProperties;
import fiap.remembered.entity.HorarioMedicamento;
import fiap.remembered.entity.Medicamento;
import fiap.remembered.entity.RegistroDose;
import fiap.remembered.repository.MedicamentoRepository;
import fiap.remembered.repository.RegistroDoseRepository;
import fiap.remembered.util.Fuso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeradorDeDosesTest {

    /** Segunda-feira, 16/11/2026, 10:00 em Sao Paulo. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 10, 0);
    private static final LocalDate HOJE = AGORA.toLocalDate();

    @Mock RegistroDoseRepository doses;
    @Mock MedicamentoRepository medicamentos;
    @Captor ArgumentCaptor<List<RegistroDose>> dosesSalvas;

    private GeradorDeDoses gerador;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(AGORA.atZone(Fuso.ZONA).toInstant(), Fuso.ZONA);
        gerador = new GeradorDeDoses(doses, medicamentos, new DosesProperties(7, 30), relogio);
    }

    @Test
    void geraSeteDiasEIgnoraOsHorariosQueJaPassaramHoje() {
        Medicamento medicamento = medicamentoComHorarios(List.of(horario("08:00", "1111111"), horario("20:00", "1111111")));

        gerador.agendar(medicamento);

        verify(doses).saveAll(dosesSalvas.capture());
        // 6 dias completos x 2 horarios + so o das 20:00 de hoje (o das 08:00 ja passou).
        assertThat(dosesSalvas.getValue()).hasSize(13);
        assertThat(dosesSalvas.getValue()).extracting(RegistroDose::getHorarioPrevisto)
                .doesNotContain(HOJE.atTime(8, 0))
                .contains(HOJE.atTime(20, 0));
    }

    @Test
    void respeitaOsDiasDaSemanaDoHorario() {
        // So segundas-feiras ("1000000"): dentro de 7 dias a partir de uma segunda ha uma unica segunda.
        Medicamento medicamento = medicamentoComHorarios(List.of(horario("20:00", "1000000")));

        gerador.agendar(medicamento);

        verify(doses).saveAll(dosesSalvas.capture());
        assertThat(dosesSalvas.getValue()).hasSize(1);
    }

    @Test
    void naoDuplicaDosesJaGeradas() {
        Medicamento medicamento = medicamentoComHorarios(List.of(horario("20:00", "1111111")));
        when(doses.horariosGeradosAPartirDe(eq(1L), any())).thenReturn(List.of(HOJE.atTime(20, 0), HOJE.plusDays(1).atTime(20, 0)));

        gerador.agendar(medicamento);

        verify(doses).saveAll(dosesSalvas.capture());
        assertThat(dosesSalvas.getValue()).hasSize(5);
    }

    @Test
    void naoGeraDosesForaDoPeriodoDeTratamento() {
        Medicamento medicamento = medicamentoComHorarios(List.of(horario("20:00", "1111111")));
        medicamento.setDataFim(HOJE.plusDays(1));

        gerador.agendar(medicamento);

        verify(doses).saveAll(dosesSalvas.capture());
        assertThat(dosesSalvas.getValue()).hasSize(2);
    }

    @Test
    void medicamentoInativoNaoGeraDoses() {
        Medicamento medicamento = medicamentoComHorarios(List.of(horario("20:00", "1111111")));
        medicamento.setAtivo(false);

        gerador.agendar(medicamento);

        verify(doses).saveAll(dosesSalvas.capture());
        assertThat(dosesSalvas.getValue()).isEmpty();
    }

    private Medicamento medicamentoComHorarios(List<HorarioMedicamento> horarios) {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(1L);
        medicamento.setDataInicio(HOJE.minusDays(10));
        medicamento.substituirHorarios(horarios);
        return medicamento;
    }

    private HorarioMedicamento horario(String hora, String dias) {
        HorarioMedicamento horario = new HorarioMedicamento();
        horario.setHora(LocalTime.parse(hora));
        horario.setDiasSemana(dias);
        return horario;
    }
}
