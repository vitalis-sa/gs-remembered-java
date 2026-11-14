package fiap.remembered.messaging.evento;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fiap.remembered.entity.Medicamento;
import fiap.remembered.enums.TipoAviso;
import fiap.remembered.enums.TipoCriador;
import fiap.remembered.messaging.EventosRouting;

import java.util.UUID;

/** Publicado pela API Java ao gravar um medicamento e consumido por ela mesma. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MedicamentoCriado(String eventId,
                                Long medicamentoId,
                                Long idosoId,
                                String medicamentoNome,
                                TipoCriador criadoPorTipo,
                                String criadoPorNome) implements NovidadeParaIdoso {

    public static MedicamentoCriado de(Medicamento medicamento) {
        return new MedicamentoCriado(
                UUID.randomUUID().toString(),
                medicamento.getId(),
                medicamento.getIdoso().getId(),
                medicamento.getNome(),
                medicamento.getCriadoPorTipo(),
                medicamento.getCriadoPorNome());
    }

    @Override
    public EventosRouting rota() {
        return EventosRouting.MEDICAMENTO_CRIADO;
    }

    @Override
    @JsonIgnore
    public TipoAviso tipoAviso() {
        return TipoAviso.MEDICAMENTO;
    }

    @Override
    @JsonIgnore
    public Long referenciaId() {
        return medicamentoId;
    }

    @Override
    @JsonIgnore
    public String titulo() {
        return "Novo remédio cadastrado";
    }

    @Override
    @JsonIgnore
    public String resumo() {
        return "%s cadastrou um remédio novo: %s".formatted(criadoPorNome, medicamentoNome);
    }
}
