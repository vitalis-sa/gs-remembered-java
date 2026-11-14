package fiap.remembered.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

/** Unico ponto de (de)serializacao JSON manual: eventos do RabbitMQ, colunas CLOB e entrada do LLM. */
@Component
@RequiredArgsConstructor
public class Json {

    private final JsonMapper mapper;

    public String escrever(Object valor) {
        return mapper.writeValueAsString(valor);
    }

    public <T> T ler(String json, Class<T> tipo) {
        return mapper.readValue(json, tipo);
    }

    public <T> Optional<T> lerOpcional(String json, Class<T> tipo) {
        return Optional.ofNullable(json)
                .filter(texto -> !texto.isBlank())
                .map(texto -> ler(texto, tipo));
    }
}
