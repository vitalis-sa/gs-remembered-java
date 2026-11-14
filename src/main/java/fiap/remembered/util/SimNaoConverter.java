package fiap.remembered.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Oracle nao tem BOOLEAN em coluna: o schema usa CHAR(1) 'S' ou 'N'. */
@Converter
public class SimNaoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String coluna) {
        return "S".equals(coluna);
    }
}
