package fiap.remembered.messaging.evento;

import fiap.remembered.messaging.EventosRouting;

/** Todo evento sabe por qual routing key viaja e tem um id unico (base da idempotencia). */
public interface EventoDeDominio {

    String eventId();

    EventosRouting rota();
}
