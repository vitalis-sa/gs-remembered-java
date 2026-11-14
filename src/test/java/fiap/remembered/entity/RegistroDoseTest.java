package fiap.remembered.entity;

import fiap.remembered.enums.OrigemDose;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistroDoseTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 8, 5);

    @Test
    void doseNasceNaoConfirmadaEConfirmaUmaVez() {
        RegistroDose dose = RegistroDose.pendente(new Medicamento(), AGORA.minusMinutes(5));
        assertThat(dose.getStatus()).isEqualTo(StatusDose.PENDENTE);

        dose.confirmar(OrigemDose.APP, AGORA);

        assertThat(dose.getStatus()).isEqualTo(StatusDose.TOMADA);
        assertThat(dose.getConfirmadaEm()).isEqualTo(AGORA);
        assertThat(dose.getOrigem()).isEqualTo(OrigemDose.APP);
    }

    @Test
    void doseTomadaOuPuladaNaoVoltaAtras() {
        RegistroDose tomada = RegistroDose.pendente(new Medicamento(), AGORA);
        tomada.confirmar(OrigemDose.APP, AGORA);
        RegistroDose pulada = RegistroDose.pendente(new Medicamento(), AGORA);
        pulada.marcarComoPulada();

        assertThatThrownBy(() -> tomada.confirmar(OrigemDose.CUIDADOR, AGORA))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("TOMADA");
        assertThatThrownBy(() -> pulada.confirmar(OrigemDose.APP, AGORA))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("PULADA");
    }
}
