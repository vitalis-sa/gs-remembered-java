package fiap.remembered.integration;

import fiap.remembered.config.PushProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

/**
 * Envia push pela API do Expo. E melhor esforco: o aviso ja esta gravado no banco, entao uma falha
 * aqui (sem token, Expo fora do ar) so vai para o log e o idoso ainda ve o aviso ao abrir o app.
 */
@Slf4j
@Service
public class PushService {

    private final RestClient restClient = RestClient.create();
    private final String url;

    public PushService(PushProperties propriedades) {
        this.url = propriedades.url();
    }

    public void enviar(String tokenDoAparelho, String titulo, String corpo) {
        Optional.ofNullable(tokenDoAparelho)
                .filter(token -> !token.isBlank())
                .ifPresent(token -> postar(token, titulo, corpo));
    }

    private void postar(String token, String titulo, String corpo) {
        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("to", token, "title", titulo, "body", corpo, "sound", "default", "priority", "high"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Push não enviado: {}", e.getMessage());
        }
    }
}
