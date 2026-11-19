package fiap.remembered.service;

import fiap.remembered.dto.response.DoseResponse;
import fiap.remembered.entity.Medicamento;
import fiap.remembered.entity.RegistroDose;
import fiap.remembered.entity.Usuario;
import fiap.remembered.enums.OrigemDose;
import fiap.remembered.enums.Perfil;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.exception.RecursoNaoEncontradoException;
import fiap.remembered.exception.RegraDeNegocioException;
import fiap.remembered.mapper.DoseMapperImpl;
import fiap.remembered.repository.RegistroDoseRepository;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.security.VinculoGuard;
import fiap.remembered.util.Fuso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoseServiceTest {

    private static final Long IDOSO_ID = 5L;
    private static final Long DOSE_ID = 100L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 19, 10, 0);

    @Mock RegistroDoseRepository doses;

    private DoseService service;

    private final UsuarioLogado idoso = new UsuarioLogado(String.valueOf(IDOSO_ID), Perfil.IDOSO, "Maria", Set.of());
    private final UsuarioLogado outroIdoso = new UsuarioLogado("6", Perfil.IDOSO, "José", Set.of());
    private final UsuarioLogado cuidadorVinculado = new UsuarioLogado("77", Perfil.CUIDADOR, "Carlos", Set.of(IDOSO_ID));

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(AGORA.atZone(Fuso.ZONA).toInstant(), Fuso.ZONA);
        service = new DoseService(doses, new DoseMapperImpl(), new VinculoGuard(), relogio);
    }

    private Medicamento losartanaDoIdoso() {
        Usuario dono = Usuario.novo("Maria", "maria@teste.dev", "hash");
        dono.setId(IDOSO_ID);
        Medicamento medicamento = new Medicamento();
        medicamento.setId(9L);
        medicamento.setNome("Losartana");
        medicamento.setIdoso(dono);
        return medicamento;
    }

    private RegistroDose doseAs(int hora) {
        return RegistroDose.pendente(losartanaDoIdoso(), AGORA.toLocalDate().atTime(hora, 0));
    }

    @Test
    void oIdosoConfirmaADoseEFicaRegistradoQueFoiPeloApp() {
        RegistroDose dose = doseAs(8);
        when(doses.buscarComMedicamento(DOSE_ID)).thenReturn(Optional.of(dose));

        DoseResponse resposta = service.confirmar(DOSE_ID, idoso);

        assertThat(resposta.status()).isEqualTo(StatusDose.TOMADA);
        assertThat(resposta.origem()).isEqualTo(OrigemDose.APP);
        assertThat(resposta.confirmadaEm()).isEqualTo(AGORA);
        assertThat(resposta.medicamentoNome()).isEqualTo("Losartana");
    }

    @Test
    void oCuidadorVinculadoTambemConfirmaEFicaRegistradoQueFoiPeloCuidador() {
        when(doses.buscarComMedicamento(DOSE_ID)).thenReturn(Optional.of(doseAs(8)));

        DoseResponse resposta = service.confirmar(DOSE_ID, cuidadorVinculado);

        assertThat(resposta.status()).isEqualTo(StatusDose.TOMADA);
        assertThat(resposta.origem()).isEqualTo(OrigemDose.CUIDADOR);
    }

    /** Dose de outra pessoa responde 404, igual a "nao existe": um 403 confirmaria que o id existe. */
    @Test
    void confirmarADoseDeOutroIdosoRespondeComoSeNaoExistisseESemAlterarADose() {
        RegistroDose dose = doseAs(8);
        when(doses.buscarComMedicamento(DOSE_ID)).thenReturn(Optional.of(dose));

        assertThatThrownBy(() -> service.confirmar(DOSE_ID, outroIdoso))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        assertThat(dose.getStatus()).isEqualTo(StatusDose.PENDENTE);
        assertThat(dose.getConfirmadaEm()).isNull();
    }

    @Test
    void confirmarUmaDoseInexistenteRespondeNaoEncontrada() {
        when(doses.buscarComMedicamento(DOSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmar(DOSE_ID, idoso))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void confirmarUmaDoseJaRegistradaRecusaComConflito() {
        RegistroDose pulada = doseAs(8);
        pulada.marcarComoPulada();
        when(doses.buscarComMedicamento(DOSE_ID)).thenReturn(Optional.of(pulada));

        assertThatThrownBy(() -> service.confirmar(DOSE_ID, idoso))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("PULADA");
    }

    @Test
    void listarSemDataTrazOsDeHojeEOFiltroPorStatusSoDeixaPassarOPedido() {
        RegistroDose pendente = doseAs(20);
        RegistroDose tomada = doseAs(8);
        tomada.confirmar(OrigemDose.APP, AGORA);
        LocalDate hoje = AGORA.toLocalDate();
        when(doses.doIdosoEntre(IDOSO_ID, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(tomada, pendente));

        List<DoseResponse> soPendentes = service.listar(IDOSO_ID, null, StatusDose.PENDENTE);
        List<DoseResponse> todas = service.listar(IDOSO_ID, null, null);

        assertThat(soPendentes).extracting(DoseResponse::status).containsExactly(StatusDose.PENDENTE);
        assertThat(todas).hasSize(2);
    }

    @Test
    void listarComDataUsaODiaPedidoEMantemAOrdemDoBanco() {
        LocalDate dia = LocalDate.of(2026, 11, 20);
        RegistroDose manha = RegistroDose.pendente(losartanaDoIdoso(), dia.atTime(8, 0));
        RegistroDose noite = RegistroDose.pendente(losartanaDoIdoso(), dia.atTime(20, 0));
        when(doses.doIdosoEntre(IDOSO_ID, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(manha, noite));

        List<DoseResponse> resposta = service.listar(IDOSO_ID, dia, null);

        assertThat(resposta).extracting(DoseResponse::horarioPrevisto)
                .containsExactly(dia.atTime(8, 0), dia.atTime(20, 0));
        verify(doses).doIdosoEntre(IDOSO_ID, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }
}
