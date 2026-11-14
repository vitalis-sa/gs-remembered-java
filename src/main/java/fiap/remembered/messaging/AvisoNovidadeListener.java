package fiap.remembered.messaging;

import com.rabbitmq.client.Channel;
import fiap.remembered.messaging.evento.ConsultaCriada;
import fiap.remembered.messaging.evento.MedicamentoCriado;
import fiap.remembered.messaging.evento.NovidadeParaIdoso;
import fiap.remembered.service.NovidadeService;
import fiap.remembered.util.Json;
import fiap.remembered.util.Tentativas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Consome {@code medicamento.criado} e {@code consulta.criada}. Confirmacao manual: a mensagem so sai
 * da fila depois de processada. Falhas tem 3 tentativas com espera crescente; esgotadas, a mensagem
 * vai para a fila de mensagens mortas (nack sem requeue) em vez de travar a fila. A idempotencia
 * por eventId esta no NovidadeService, entao reentrega nao duplica aviso.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvisoNovidadeListener {

    private static final int MAXIMO_DE_TENTATIVAS = 3;
    private static final long ESPERA_INICIAL_MS = 500;

    private static final Map<String, Class<? extends NovidadeParaIdoso>> TIPO_POR_ROTA = Map.of(
            EventosRouting.MEDICAMENTO_CRIADO.getChave(), MedicamentoCriado.class,
            EventosRouting.CONSULTA_CRIADA.getChave(), ConsultaCriada.class);

    private final NovidadeService novidadeService;
    private final Json json;

    @RabbitListener(queues = RabbitConfig.FILA_AVISO_NOVIDADE)
    public void receber(Message mensagem, Channel canal) throws IOException {
        long tag = mensagem.getMessageProperties().getDeliveryTag();
        try {
            Tentativas.executar(MAXIMO_DE_TENTATIVAS, ESPERA_INICIAL_MS, () -> processar(mensagem));
            canal.basicAck(tag, false);
        } catch (RuntimeException e) {
            log.error("Mensagem enviada à fila de mensagens mortas após {} tentativas: {}", MAXIMO_DE_TENTATIVAS, e.getMessage());
            canal.basicNack(tag, false, false);
        }
    }

    private void processar(Message mensagem) {
        String rota = mensagem.getMessageProperties().getReceivedRoutingKey();
        Class<? extends NovidadeParaIdoso> tipo = Optional.ofNullable(TIPO_POR_ROTA.get(rota))
                .orElseThrow(() -> new IllegalStateException("Rota sem tratamento: " + rota));
        String corpo = new String(mensagem.getBody(), StandardCharsets.UTF_8);
        novidadeService.registrar(json.ler(corpo, tipo));
    }
}
