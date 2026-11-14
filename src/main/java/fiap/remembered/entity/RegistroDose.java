package fiap.remembered.entity;

import fiap.remembered.enums.OrigemDose;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Uma dose prevista. A regra do ciclo de vida mora aqui: quem a chama nao precisa conhecer as transicoes. */
@Entity
@Table(name = "TB_REGISTRO_DOSE")
@Getter
@NoArgsConstructor
public class RegistroDose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false, updatable = false)
    private Medicamento medicamento;

    @Column(name = "horario_previsto", nullable = false, updatable = false)
    private LocalDateTime horarioPrevisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusDose status = StatusDose.PENDENTE;

    @Column(name = "confirmada_em")
    private LocalDateTime confirmadaEm;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private OrigemDose origem;

    public static RegistroDose pendente(Medicamento medicamento, LocalDateTime horarioPrevisto) {
        RegistroDose dose = new RegistroDose();
        dose.medicamento = medicamento;
        dose.horarioPrevisto = horarioPrevisto;
        return dose;
    }

    public void confirmar(OrigemDose origemDaConfirmacao, LocalDateTime agora) {
        if (!status.podeSerConfirmada()) {
            throw new RegraDeNegocioException("A dose já foi registrada como " + status);
        }
        status = StatusDose.TOMADA;
        confirmadaEm = agora;
        origem = origemDaConfirmacao;
    }

    public void marcarComoPulada() {
        status = StatusDose.PULADA;
    }
}
