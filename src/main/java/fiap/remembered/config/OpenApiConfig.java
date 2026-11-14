package fiap.remembered.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = OpenApiConfig.ESQUEMA_JWT), info = @Info(
        title = "Remembered - API Java",
        version = "v1",
        description = "Medicamentos, doses, avisos e relatorio de adesao do idoso. "
                + "Autenticacao: Bearer JWT (idoso emitido por esta API; cuidador emitido pela API .NET)."))
@SecurityScheme(name = OpenApiConfig.ESQUEMA_JWT, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    public static final String ESQUEMA_JWT = "bearerAuth";
}
