package fiap.remembered.service;

import fiap.remembered.dto.request.HorarioRequest;
import fiap.remembered.dto.request.MedicamentoRequest;
import fiap.remembered.dto.response.MedicamentoResponse;
import fiap.remembered.entity.Medicamento;
import fiap.remembered.entity.Usuario;
import fiap.remembered.enums.Perfil;
import fiap.remembered.enums.TipoCriador;
import fiap.remembered.exception.RecursoNaoEncontradoException;
import fiap.remembered.exception.RequisicaoInvalidaException;
import fiap.remembered.mapper.MedicamentoMapperImpl;
import fiap.remembered.messaging.evento.MedicamentoCriado;
import fiap.remembered.repository.MedicamentoRepository;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.security.VinculoGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicamentoServiceTest {

    private static final Long IDOSO_ID = 5L;
    private static final Long MEDICAMENTO_ID = 9L;
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 19);

    @Mock MedicamentoRepository medicamentos;
    @Mock UsuarioRepository usuarios;
    @Mock GeradorDeDoses geradorDeDoses;
    @Mock ApplicationEventPublisher eventos;

    private MedicamentoService service;

    private final UsuarioLogado idoso = new UsuarioLogado(String.valueOf(IDOSO_ID), Perfil.IDOSO, "Maria", Set.of());
    private final UsuarioLogado outroIdoso = new UsuarioLogado("6", Perfil.IDOSO, "José", Set.of());
    private final UsuarioLogado cuidador = new UsuarioLogado("77", Perfil.CUIDADOR, "Carlos", Set.of(IDOSO_ID));

    @BeforeEach
    void preparar() {
        service = new MedicamentoService(medicamentos, usuarios, new MedicamentoMapperImpl(), geradorDeDoses,
                new VinculoGuard(), eventos);
    }

    private Usuario donoDoMedicamento() {
        Usuario maria = Usuario.novo("Maria", "maria@teste.dev", "hash");
        maria.setId(IDOSO_ID);
        return maria;
    }

    private Medicamento medicamentoSalvo() {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(MEDICAMENTO_ID);
        medicamento.setNome("Losartana");
        medicamento.setDataInicio(INICIO);
        medicamento.setIdoso(donoDoMedicamento());
        return medicamento;
    }

    private MedicamentoRequest pedido(String nome, LocalDate dataFim) {
        return new MedicamentoRequest(nome, "50 mg", "Comprimido", "azul", "redondo", true, 3, INICIO, dataFim,
                true, "Tomar com água", null,
                List.of(new HorarioRequest("08:00", "1111111"), new HorarioRequest("20:00", null)));
    }

    @Test
    void cuidadorCadastraEOMedicamentoFicaMarcadoComoCriadoPorEleAgendaAsDosesEPublicaOEvento() {
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(donoDoMedicamento());
        when(medicamentos.save(any(Medicamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        MedicamentoResponse resposta = service.criar(IDOSO_ID, pedido("Losartana", null), cuidador);

        ArgumentCaptor<Medicamento> salvo = ArgumentCaptor.forClass(Medicamento.class);
        verify(medicamentos).save(salvo.capture());
        assertThat(salvo.getValue().getCriadoPorTipo()).isEqualTo(TipoCriador.CUIDADOR);
        assertThat(salvo.getValue().getCriadoPorNome()).isEqualTo("Carlos");
        assertThat(salvo.getValue().getHorarios()).hasSize(2);
        assertThat(resposta.idosoId()).isEqualTo(IDOSO_ID);
        assertThat(resposta.horarios()).hasSize(2);

        verify(geradorDeDoses).agendar(salvo.getValue());
        ArgumentCaptor<MedicamentoCriado> evento = ArgumentCaptor.forClass(MedicamentoCriado.class);
        verify(eventos).publishEvent(evento.capture());
        assertThat(evento.getValue().criadoPorTipo()).isEqualTo(TipoCriador.CUIDADOR);
        assertThat(evento.getValue().idosoId()).isEqualTo(IDOSO_ID);
    }

    @Test
    void oIdosoQueCadastraFicaComoCriadorDoProprioMedicamento() {
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(donoDoMedicamento());
        when(medicamentos.save(any(Medicamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        service.criar(IDOSO_ID, pedido("Losartana", null), idoso);

        ArgumentCaptor<Medicamento> salvo = ArgumentCaptor.forClass(Medicamento.class);
        verify(medicamentos).save(salvo.capture());
        assertThat(salvo.getValue().getCriadoPorTipo()).isEqualTo(TipoCriador.IDOSO);
    }

    @Test
    void dataDeFimAnteriorAoInicioRecusaComRequisicaoInvalidaSemGravarNada() {
        MedicamentoRequest pedido = pedido("Losartana", INICIO.minusDays(1));

        assertThatThrownBy(() -> service.criar(IDOSO_ID, pedido, idoso))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessageContaining("data de fim");

        verify(medicamentos, never()).save(any(Medicamento.class));
        verifyNoInteractions(geradorDeDoses, eventos);
    }

    @Test
    void atualizarTrocaOsDadosERegeraAsDosesFuturas() {
        Medicamento existente = medicamentoSalvo();
        when(medicamentos.obter(MEDICAMENTO_ID, "Medicamento")).thenReturn(existente);

        MedicamentoResponse resposta = service.atualizar(MEDICAMENTO_ID, pedido("Losartana Potássica", null), idoso);

        assertThat(existente.getNome()).isEqualTo("Losartana Potássica");
        assertThat(existente.getHorarios()).hasSize(2);
        assertThat(resposta.nome()).isEqualTo("Losartana Potássica");
        verify(geradorDeDoses).reagendar(existente);
    }

    /** Medicamento de outra pessoa responde 404, igual a "nao existe": um 403 confirmaria que o id existe. */
    @Test
    void medicamentoDeOutroIdosoRespondeNaoEncontradoEmBuscarAtualizarEExcluir() {
        Medicamento alheio = medicamentoSalvo();
        when(medicamentos.obter(MEDICAMENTO_ID, "Medicamento")).thenReturn(alheio);

        assertThatThrownBy(() -> service.buscar(MEDICAMENTO_ID, outroIdoso))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.atualizar(MEDICAMENTO_ID, pedido("X", null), outroIdoso))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.excluir(MEDICAMENTO_ID, outroIdoso))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(medicamentos, never()).delete(any(Medicamento.class));
        verifyNoInteractions(geradorDeDoses);
    }

    @Test
    void oCuidadorVinculadoExcluiOMedicamentoDoIdoso() {
        Medicamento existente = medicamentoSalvo();
        when(medicamentos.obter(MEDICAMENTO_ID, "Medicamento")).thenReturn(existente);

        service.excluir(MEDICAMENTO_ID, cuidador);

        verify(medicamentos).delete(existente);
    }
}
