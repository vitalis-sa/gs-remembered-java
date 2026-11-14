package fiap.remembered.entity;

import fiap.remembered.enums.TipoCriador;
import fiap.remembered.util.SimNaoConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TB_MEDICAMENTO")
@Getter
@Setter
@NoArgsConstructor
public class Medicamento {

    private static final int LIMITE_PULOS_PADRAO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoso_id", nullable = false, updatable = false)
    private Usuario idoso;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 60)
    private String dosagem;

    @Column(length = 40)
    private String forma;

    @Column(name = "cor_icone", length = 20)
    private String corIcone;

    @Column(name = "formato_icone", length = 20)
    private String formatoIcone;

    /** Medicamento critico alerta o cuidador na primeira dose pulada. */
    @Convert(converter = SimNaoConverter.class)
    @Column(nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private boolean critico;

    @Column(name = "limite_pulos", nullable = false)
    private int limitePulos = LIMITE_PULOS_PADRAO;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Convert(converter = SimNaoConverter.class)
    @Column(nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private boolean ativo = true;

    @Column(length = 500)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "criado_por_tipo", nullable = false, updatable = false, length = 10)
    private TipoCriador criadoPorTipo;

    @Column(name = "criado_por_nome", nullable = false, updatable = false, length = 120)
    private String criadoPorNome;

    /** BatchSize: uma pagina de medicamentos carrega os horarios em 1 consulta extra, nao em N. */
    @BatchSize(size = 50)
    @OrderBy("hora")
    @OneToMany(mappedBy = "medicamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioMedicamento> horarios = new ArrayList<>();

    /** Um medicamento so gera doses enquanto esta ativo e dentro do periodo de tratamento. */
    public boolean vigenteEm(LocalDate dia) {
        return ativo && !dia.isBefore(dataInicio) && (dataFim == null || !dia.isAfter(dataFim));
    }

    public void substituirHorarios(List<HorarioMedicamento> novos) {
        horarios.clear();
        novos.forEach(this::adicionarHorario);
    }

    private void adicionarHorario(HorarioMedicamento horario) {
        horario.setMedicamento(this);
        horarios.add(horario);
    }
}
