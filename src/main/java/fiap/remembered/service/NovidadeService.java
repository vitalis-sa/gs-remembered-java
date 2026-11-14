package fiap.remembered.service;

import fiap.remembered.entity.Aviso;
import fiap.remembered.entity.Usuario;
import fiap.remembered.integration.PushService;
import fiap.remembered.messaging.evento.NovidadeParaIdoso;
import fiap.remembered.repository.AvisoRepository;
import fiap.remembered.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Efeito dos eventos medicamento.criado e consulta.criada: quando o cuidador cadastrou, o idoso
 * ganha um aviso gravado e um push. Cadastros do proprio idoso (inclusive pelo assistente) nao geram aviso.
 *
 * Idempotente por eventId: uma reentrega do broker nao duplica o aviso (consulta previa mais a
 * restricao UNIQUE como garantia contra duas entregas simultaneas).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovidadeService {

    private final AvisoRepository avisos;
    private final UsuarioRepository usuarios;
    private final PushService pushService;

    public void registrar(NovidadeParaIdoso novidade) {
        Optional.of(novidade)
                .filter(NovidadeParaIdoso::criadaPorCuidador)
                .filter(evento -> !avisos.existsByEventoId(evento.eventId()))
                .flatMap(this::gravarAviso)
                .ifPresent(aviso -> pushService.enviar(aviso.getIdoso().getPushToken(), aviso.getTitulo(), aviso.getResumo()));
    }

    private Optional<Aviso> gravarAviso(NovidadeParaIdoso novidade) {
        Usuario idoso = usuarios.obter(novidade.idosoId(), "Idoso");
        try {
            return Optional.of(avisos.saveAndFlush(Aviso.builder()
                    .idoso(idoso)
                    .tipo(novidade.tipoAviso())
                    .referenciaId(novidade.referenciaId())
                    .titulo(novidade.titulo())
                    .resumo(novidade.resumo())
                    .criadoPorNome(novidade.criadoPorNome())
                    .eventoId(novidade.eventId())
                    .build()));
        } catch (DataIntegrityViolationException e) {
            log.info("Evento {} já processado por outra entrega", novidade.eventId());
            return Optional.empty();
        }
    }
}
