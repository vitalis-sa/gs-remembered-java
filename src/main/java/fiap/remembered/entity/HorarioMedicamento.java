package fiap.remembered.entity;

import fiap.remembered.util.HoraConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "TB_HORARIO_MEDICAMENTO")
@Getter
@Setter
@NoArgsConstructor
public class HorarioMedicamento {

    private static final char DIA_ATIVO = '1';

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Convert(converter = HoraConverter.class)
    @Column(nullable = false, length = 5)
    private LocalTime hora;

    /** Sete digitos 0/1, da segunda (posicao 0) ao domingo (posicao 6): "1111111" e todos os dias. */
    @Column(name = "dias_semana", nullable = false, length = 7)
    private String diasSemana;

    public boolean ocorreEm(DayOfWeek dia) {
        return diasSemana.charAt(dia.getValue() - 1) == DIA_ATIVO;
    }

    public LocalDateTime em(LocalDate dia) {
        return dia.atTime(hora);
    }
}
