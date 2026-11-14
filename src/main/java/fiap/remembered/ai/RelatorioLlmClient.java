package fiap.remembered.ai;

import fiap.remembered.config.IaProperties;
import fiap.remembered.dto.response.DadosAdesao;
import fiap.remembered.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Pede ao LLM (Groq, pelo Spring AI) o texto do relatorio. O prompt de sistema e fixo e vive em
 * resources/prompts; os numeros chegam como JSON no prompt do usuario. A chamada tem prazo: se o
 * LLM falhar ou passar do limite, devolve vazio e o relatorio sai so com os numeros.
 */
@Slf4j
@Component
public class RelatorioLlmClient {

    private final ChatClient chatClient;
    private final String promptDoSistema;
    private final Duration prazo;
    private final Json json;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public RelatorioLlmClient(ChatClient.Builder chatClientBuilder,
                              Json json,
                              IaProperties propriedades,
                              @Value("classpath:prompts/relatorio-adesao-system.st") Resource prompt) throws IOException {
        this.chatClient = chatClientBuilder.build();
        this.json = json;
        this.prazo = propriedades.timeout();
        this.promptDoSistema = prompt.getContentAsString(StandardCharsets.UTF_8);
    }

    public Optional<AnaliseAdesaoLlm> analisar(DadosAdesao dados) {
        Future<AnaliseAdesaoLlm> chamada = executor.submit(() -> consultar(dados));
        try {
            return Optional.ofNullable(chamada.get(prazo.toMillis(), TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return semAnalise(chamada, e);
        } catch (TimeoutException | ExecutionException | RuntimeException e) {
            return semAnalise(chamada, e);
        }
    }

    private AnaliseAdesaoLlm consultar(DadosAdesao dados) {
        return chatClient.prompt()
                .system(promptDoSistema)
                // O JSON tem chaves, que o Spring AI leria como variaveis de template: vai como parametro.
                .user(usuario -> usuario.text("Dados de adesão do período: {dados}").param("dados", json.escrever(dados)))
                .call()
                .entity(AnaliseAdesaoLlm.class);
    }

    private Optional<AnaliseAdesaoLlm> semAnalise(Future<AnaliseAdesaoLlm> chamada, Exception causa) {
        chamada.cancel(true);
        log.warn("LLM indisponível; relatório segue só com os números: {}", causa.toString());
        return Optional.empty();
    }
}
