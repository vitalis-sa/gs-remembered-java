package fiap.remembered.ai;

import fiap.remembered.config.IaProperties;
import fiap.remembered.dto.response.DadosAdesao;
import fiap.remembered.util.Json;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ByteArrayResource;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Usa um ChatModel simulado: nenhum teste chama o Groq de verdade. */
class RelatorioLlmClientTest {

    private static final DadosAdesao DADOS = new DadosAdesao(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 16), null, List.of());

    @Test
    void quandoOLlmFalhaDevolveVazioParaOServiceSalvarSoOsNumeros() throws IOException {
        ChatModel modelo = mock(ChatModel.class);
        when(modelo.call(any(Prompt.class))).thenThrow(new IllegalStateException("Groq fora do ar"));

        assertThat(clienteCom(modelo, Duration.ofSeconds(5)).analisar(DADOS)).isEmpty();
    }

    @Test
    void quandoOLlmPassaDoPrazoDevolveVazio() throws IOException {
        ChatModel modelo = mock(ChatModel.class);
        when(modelo.call(any(Prompt.class))).thenAnswer(invocacao -> {
            Thread.sleep(3_000);
            return null;
        });

        assertThat(clienteCom(modelo, Duration.ofMillis(200)).analisar(DADOS)).isEmpty();
    }

    private RelatorioLlmClient clienteCom(ChatModel modelo, Duration prazo) throws IOException {
        return new RelatorioLlmClient(
                ChatClient.builder(modelo),
                new Json(JsonMapper.builder().build()),
                new IaProperties(prazo),
                new ByteArrayResource("prompt de teste".getBytes()));
    }
}
