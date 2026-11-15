package fiap.remembered.config;

import fiap.remembered.dto.request.MedicamentoRequest;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O Spring AI traz versoes antigas do swagger-annotations, e o Maven escolhe a mais proxima.
 * Com a anotacao @Schema antiga no classpath, o swagger-core do springdoc falha com
 * NoSuchMethodError ao ler um @Schema, e o /api-docs (e o Swagger UI) respondem 500.
 */
class DocumentacaoOpenApiTest {

    @Test
    void swaggerCoreLeOSchemaDeUmCampoDoDto() {
        Schema anotacao = Arrays.stream(MedicamentoRequest.class.getDeclaredFields())
                .map(campo -> campo.getAnnotation(Schema.class))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();

        var schema = AnnotationsUtils.getSchemaFromAnnotation(anotacao, null);

        assertThat(schema).isPresent();
        assertThat(schema.get().getDescription()).isEqualTo(anotacao.description());
    }
}
