package fiap.remembered.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia: exchange topic {@code remembered.eventos} -> fila {@code idoso.aviso-novidade} (durável) com fila
 * de mensagens mortas. A fila recebe os dois eventos que viram aviso para o idoso. A fila e declarada
 * aqui (idempotente) para a publicacao nunca cair no vazio se esta API subir antes da .NET.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "remembered.eventos";
    public static final String FILA_AVISO_NOVIDADE = "idoso.aviso-novidade";
    public static final String FILA_AVISO_NOVIDADE_DLQ = FILA_AVISO_NOVIDADE + ".dlq";

    @Bean
    TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue avisoNovidadeQueue() {
        return QueueBuilder.durable(FILA_AVISO_NOVIDADE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FILA_AVISO_NOVIDADE_DLQ)
                .build();
    }

    @Bean
    Queue avisoNovidadeDeadLetterQueue() {
        return QueueBuilder.durable(FILA_AVISO_NOVIDADE_DLQ).build();
    }

    @Bean
    Binding bindingMedicamentoCriado(Queue avisoNovidadeQueue, TopicExchange eventosExchange) {
        return BindingBuilder.bind(avisoNovidadeQueue).to(eventosExchange).with(EventosRouting.MEDICAMENTO_CRIADO.getChave());
    }

    @Bean
    Binding bindingConsultaCriada(Queue avisoNovidadeQueue, TopicExchange eventosExchange) {
        return BindingBuilder.bind(avisoNovidadeQueue).to(eventosExchange).with(EventosRouting.CONSULTA_CRIADA.getChave());
    }
}
