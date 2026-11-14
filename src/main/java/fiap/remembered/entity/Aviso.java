package fiap.remembered.entity;

import fiap.remembered.enums.TipoAviso;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "TB_AVISO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoso_id", nullable = false, updatable = false)
    private Usuario idoso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TipoAviso tipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 300)
    private String resumo;

    @Column(name = "criado_por_nome", nullable = false, length = 120)
    private String criadoPorNome;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "lido_em")
    private LocalDateTime lidoEm;

    /** Chave de idempotencia: o mesmo evento entregue duas vezes gera um unico aviso. */
    @Column(name = "evento_id", nullable = false, updatable = false, length = 64)
    private String eventoId;

    @PrePersist
    void aoCriar() {
        criadoEm = Optional.ofNullable(criadoEm).orElseGet(LocalDateTime::now);
    }

    /** Idempotente: ler de novo nao muda o instante da primeira leitura. */
    public void marcarComoLido(LocalDateTime agora) {
        lidoEm = Optional.ofNullable(lidoEm).orElse(agora);
    }
}
