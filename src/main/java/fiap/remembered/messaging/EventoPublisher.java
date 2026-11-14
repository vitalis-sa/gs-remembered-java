package fiap.remembered.messaging;

import fiap.remembered.messaging.evento.EventoDeDominio;
import fiap.remembered.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica qualquer evento de dominio, mas so depois que a transacao foi confirmada: publicar antes
 * mandaria ao outro lado um fato que um rollback poderia desfazer. Se o broker falhar, o dado
 * continua gravado e o erro vai para o log; o pedido do usuario ja foi atendido e nao vira 500.
 *
 * Os services so chamam {@code ApplicationEventPublisher.publishEvent(evento)}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventoPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final Json json;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(EventoDeDominio evento) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    evento.rota().getChave(),
                    json.escrever(evento),
                    mensagem -> marcar(mensagem, evento));
            log.info("Evento publicado: {} ({})", evento.rota().getChave(), evento.eventId());
        } catch (RuntimeException e) {
            log.error("Evento NÃO publicado: {} ({}): {}", evento.rota().getChave(), evento.eventId(), e.getMessage());
        }
    }

    private Message marcar(Message mensagem, EventoDeDominio evento) {
        MessageProperties propriedades = mensagem.getMessageProperties();
        propriedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        propriedades.setMessageId(evento.eventId());
        return mensagem;
    }
}
