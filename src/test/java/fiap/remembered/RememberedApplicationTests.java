package fiap.remembered;

import fiap.remembered.util.Fuso;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O contexto completo precisa de Oracle e RabbitMQ no ar, entao nao e carregado nos testes unitarios.
 * Aqui fica a garantia do fuso unico da aplicacao.
 */
class 	RememberedApplicationTests {

	@Test
	void fixaOFusoDeSaoPauloAoCarregarAAplicacao() {
		new RememberedApplication();

		assertThat(TimeZone.getDefault().toZoneId()).isEqualTo(Fuso.ZONA);
	}
}
