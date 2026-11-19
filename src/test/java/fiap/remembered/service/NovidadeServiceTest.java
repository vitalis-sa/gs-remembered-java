package fiap.remembered.service;

import fiap.remembered.entity.Aviso;
import fiap.remembered.entity.Usuario;
import fiap.remembered.enums.TipoAviso;
import fiap.remembered.enums.TipoCriador;
import fiap.remembered.integration.PushService;
import fiap.remembered.messaging.evento.ConsultaCriada;
import fiap.remembered.messaging.evento.MedicamentoCriado;
import fiap.remembered.repository.AvisoRepository;
import fiap.remembered.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NovidadeServiceTest {

    private static final Long IDOSO_ID = 5L;
    private static final String PUSH_TOKEN = "ExponentPushToken[teste]";

    @Mock AvisoRepository avisos;
    @Mock UsuarioRepository usuarios;
    @Mock PushService pushService;

    private NovidadeService service;

    @BeforeEach
    void preparar() {
        service = new NovidadeService(avisos, usuarios, pushService);
    }

    private Usuario maria() {
        Usuario usuario = Usuario.novo("Maria", "maria@teste.dev", "hash");
        usuario.setId(IDOSO_ID);
        usuario.setPushToken(PUSH_TOKEN);
        return usuario;
    }

    private MedicamentoCriado medicamentoCriadoPor(TipoCriador criador, String eventId) {
        return new MedicamentoCriado(eventId, 3L, IDOSO_ID, "Losartana", criador, "Carlos");
    }

    @Test
    void medicamentoCadastradoPeloCuidadorGravaOAvisoEAvisaOIdosoPorPush() {
        when(avisos.existsByEventoId("evento-1")).thenReturn(false);
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(maria());
        when(avisos.saveAndFlush(any(Aviso.class))).thenAnswer(chamada -> chamada.getArgument(0));

        service.registrar(medicamentoCriadoPor(TipoCriador.CUIDADOR, "evento-1"));

        ArgumentCaptor<Aviso> captor = ArgumentCaptor.forClass(Aviso.class);
        verify(avisos).saveAndFlush(captor.capture());
        Aviso aviso = captor.getValue();
        assertThat(aviso.getTipo()).isEqualTo(TipoAviso.MEDICAMENTO);
        assertThat(aviso.getReferenciaId()).isEqualTo(3L);
        assertThat(aviso.getEventoId()).isEqualTo("evento-1");
        assertThat(aviso.getCriadoPorNome()).isEqualTo("Carlos");
        verify(pushService).enviar(PUSH_TOKEN, "Novo remédio cadastrado", "Carlos cadastrou um remédio novo: Losartana");
    }

    @Test
    void consultaMarcadaPeloCuidadorGeraUmAvisoDoTipoConsulta() {
        ConsultaCriada consulta = new ConsultaCriada("evento-2", 8L, IDOSO_ID, "Cardiologia",
                LocalDateTime.of(2026, 11, 20, 14, 0), TipoCriador.CUIDADOR, "Carlos");
        when(avisos.existsByEventoId("evento-2")).thenReturn(false);
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(maria());
        when(avisos.saveAndFlush(any(Aviso.class))).thenAnswer(chamada -> chamada.getArgument(0));

        service.registrar(consulta);

        ArgumentCaptor<Aviso> captor = ArgumentCaptor.forClass(Aviso.class);
        verify(avisos).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoAviso.CONSULTA);
        verify(pushService).enviar(PUSH_TOKEN, "Nova consulta marcada", "Carlos marcou uma consulta de Cardiologia");
    }

    /** Cadastro do proprio idoso (inclusive pelo assistente) nao gera aviso: ele ja sabe o que fez. */
    @Test
    void cadastroFeitoPeloProprioIdosoNaoGeraAvisoNemPush() {
        service.registrar(medicamentoCriadoPor(TipoCriador.IDOSO, "evento-3"));
        service.registrar(medicamentoCriadoPor(TipoCriador.CHAT, "evento-4"));

        verifyNoInteractions(avisos, usuarios, pushService);
    }

    /** O broker pode entregar a mesma mensagem duas vezes: o segundo processamento nao duplica o aviso. */
    @Test
    void eventoJaProcessadoNaoDuplicaOAvisoNemOPush() {
        when(avisos.existsByEventoId("evento-1")).thenReturn(true);

        service.registrar(medicamentoCriadoPor(TipoCriador.CUIDADOR, "evento-1"));

        verify(avisos, never()).saveAndFlush(any(Aviso.class));
        verifyNoInteractions(usuarios, pushService);
    }

    /** Duas entregas ao mesmo tempo: a restricao UNIQUE barra a segunda, que termina sem erro e sem push. */
    @Test
    void entregaSimultaneaBarradaPeloBancoTerminaSemErroESemPushRepetido() {
        when(avisos.existsByEventoId("evento-1")).thenReturn(false);
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(maria());
        when(avisos.saveAndFlush(any(Aviso.class))).thenThrow(new DataIntegrityViolationException("evento_id duplicado"));

        service.registrar(medicamentoCriadoPor(TipoCriador.CUIDADOR, "evento-1"));

        verifyNoInteractions(pushService);
    }
}
