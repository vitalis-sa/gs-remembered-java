package fiap.remembered.messaging.evento;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fiap.remembered.enums.TipoAviso;
import fiap.remembered.enums.TipoCriador;
import fiap.remembered.messaging.EventosRouting;

import java.time.LocalDateTime;

/** Publicado pela API .NET ao gravar uma consulta; esta API so consome. Campos extras sao ignorados. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsultaCriada(String eventId,
                             Long consultaId,
                             Long idosoId,
                             String especialidade,
                             LocalDateTime dataHora,
                             TipoCriador criadoPorTipo,
                             String criadoPorNome) implements NovidadeParaIdoso {

    @Override
    public EventosRouting rota() {
        return EventosRouting.CONSULTA_CRIADA;
    }

    @Override
    @JsonIgnore
    public TipoAviso tipoAviso() {
        return TipoAviso.CONSULTA;
    }

    @Override
    @JsonIgnore
    public Long referenciaId() {
        return consultaId;
    }

    @Override
    @JsonIgnore
    public String titulo() {
        return "Nova consulta marcada";
    }

    @Override
    @JsonIgnore
    public String resumo() {
        return "%s marcou uma consulta de %s".formatted(criadoPorNome, especialidade);
    }
}
