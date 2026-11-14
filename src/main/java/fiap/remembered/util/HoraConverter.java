package fiap.remembered.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** A coluna guarda a hora como texto HH24:MI; a entidade trabalha com LocalTime. */
@Converter
public class HoraConverter implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalTime hora) {
        return Optional.ofNullable(hora).map(HH_MM::format).orElse(null);
    }

    @Override
    public LocalTime convertToEntityAttribute(String coluna) {
        return Optional.ofNullable(coluna).map(texto -> LocalTime.parse(texto, HH_MM)).orElse(null);
    }
}
