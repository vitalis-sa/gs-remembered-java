package fiap.remembered.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TipoCriadorTest {

    @Test
    void criadorEOPerfilDeQuemChama() {
        assertThat(TipoCriador.de(Perfil.IDOSO, null)).isEqualTo(TipoCriador.IDOSO);
        assertThat(TipoCriador.de(Perfil.CUIDADOR, null)).isEqualTo(TipoCriador.CUIDADOR);
    }

    @Test
    void soOIdosoPodeDeclararQueOCadastroVeioDoAssistente() {
        assertThat(TipoCriador.de(Perfil.IDOSO, TipoCriador.CHAT)).isEqualTo(TipoCriador.CHAT);
        assertThat(TipoCriador.de(Perfil.CUIDADOR, TipoCriador.CHAT)).isEqualTo(TipoCriador.CUIDADOR);
    }

    @Test
    void severidadeDesconhecidaNaoAlarma() {
        assertThat(Severidade.deTexto("ALTA").geraEvento()).isTrue();
        assertThat(Severidade.deTexto(" media ").geraEvento()).isTrue();
        assertThat(Severidade.deTexto("NENHUM").geraEvento()).isFalse();
        assertThat(Severidade.deTexto(null)).isEqualTo(Severidade.NENHUM);
        assertThat(Severidade.deTexto("???")).isEqualTo(Severidade.NENHUM);
    }
}
