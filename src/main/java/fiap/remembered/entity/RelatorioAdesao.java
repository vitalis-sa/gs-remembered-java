package fiap.remembered.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_RELATORIO_ADESAO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioAdesao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoso_id", nullable = false, updatable = false)
    private Usuario idoso;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;

    @Column(name = "percentual_adesao", precision = 5, scale = 2)
    private BigDecimal percentualAdesao;

    /** JSON com resumo, pontosDeAtencao e sugestaoParaFamilia. Nulo quando o LLM falhou. */
    @Lob
    @Column(name = "texto_llm")
    private String textoLlm;

    /** JSON com os numeros calculados no Oracle: o que o LLM recebeu e o que a API devolve. */
    @Lob
    @Column(name = "dados_json", nullable = false)
    private String dadosJson;

    @Column(name = "gerado_em", nullable = false, updatable = false)
    private LocalDateTime geradoEm;
}
