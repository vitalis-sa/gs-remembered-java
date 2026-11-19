package fiap.remembered.service;

import fiap.remembered.config.PareamentoProperties;
import fiap.remembered.dto.response.CodigoPareamentoResponse;
import fiap.remembered.dto.response.PareamentoResponse;
import fiap.remembered.entity.Usuario;
import fiap.remembered.exception.CodigoPareamentoInvalidoException;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.util.Fuso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PareamentoServiceTest {

    private static final Long IDOSO_ID = 5L;
    private static final int VALIDADE_MINUTOS = 15;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 19, 10, 0);

    @Mock UsuarioRepository usuarios;

    private PareamentoService service;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(AGORA.atZone(Fuso.ZONA).toInstant(), Fuso.ZONA);
        service = new PareamentoService(usuarios, new PareamentoProperties(VALIDADE_MINUTOS), relogio);
    }

    private Usuario maria() {
        Usuario usuario = Usuario.novo("Maria", "maria@teste.dev", "hash");
        usuario.setId(IDOSO_ID);
        return usuario;
    }

    @Test
    void gerarCodigoGravaUmCodigoDeSeisCaracteresQueExpiraEmQuinzeMinutos() {
        Usuario maria = maria();
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(maria);
        when(usuarios.existsByCodigoPareamento(anyString())).thenReturn(false);

        CodigoPareamentoResponse resposta = service.gerarCodigo(IDOSO_ID);

        // O alfabeto do codigo nao tem I, L nem O (confundem com 1 e 0 na hora de digitar).
        assertThat(resposta.codigo()).matches("[A-HJKMNP-Z2-9]{6}");
        assertThat(resposta.expiraEm()).isEqualTo(AGORA.plusMinutes(VALIDADE_MINUTOS));
        assertThat(maria.getCodigoPareamento()).isEqualTo(resposta.codigo());
        assertThat(maria.getCodigoExpiraEm()).isEqualTo(AGORA.plusMinutes(VALIDADE_MINUTOS));
    }

    @Test
    void validarAceitaOCodigoComEspacosELetrasMinusculasEDevolveOIdoso() {
        when(usuarios.findByCodigoPareamento("ABC234")).thenReturn(Optional.of(maria()));
        when(usuarios.consumirCodigo(IDOSO_ID, "ABC234", AGORA)).thenReturn(1);

        PareamentoResponse resposta = service.validar("  abc234 ");

        assertThat(resposta.idosoId()).isEqualTo(IDOSO_ID);
        assertThat(resposta.nome()).isEqualTo("Maria");
    }

    @Test
    void validarUmCodigoQueNaoExisteRecusaSemTentarConsumir() {
        when(usuarios.findByCodigoPareamento("ZZZ999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validar("ZZZ999"))
                .isInstanceOf(CodigoPareamentoInvalidoException.class)
                .hasMessage("Código de pareamento inválido ou expirado");

        verify(usuarios, never()).consumirCodigo(anyLong(), anyString(), any(LocalDateTime.class));
    }

    /**
     * O consumo e um UPDATE so: codigo expirado, ou ja usado por outro cuidador no mesmo instante,
     * afeta zero linhas, e a resposta e a mesma de um codigo inexistente.
     */
    @Test
    void validarUmCodigoExpiradoOuJaConsumidoRecusa() {
        when(usuarios.findByCodigoPareamento("ABC234")).thenReturn(Optional.of(maria()));
        when(usuarios.consumirCodigo(eq(IDOSO_ID), eq("ABC234"), any(LocalDateTime.class))).thenReturn(0);

        assertThatThrownBy(() -> service.validar("ABC234"))
                .isInstanceOf(CodigoPareamentoInvalidoException.class);
    }
}
