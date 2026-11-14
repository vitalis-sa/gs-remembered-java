package fiap.remembered.controller;

import fiap.remembered.config.Rotas;
import fiap.remembered.dto.request.ValidarCodigoRequest;
import fiap.remembered.dto.response.PareamentoResponse;
import fiap.remembered.service.PareamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Rotas.BASE + "/pareamento")
@RequiredArgsConstructor
@Tag(name = "Pareamento", description = "Somente CUIDADOR (chamado pela API .NET com o token dele)")
public class PareamentoController {

    private final PareamentoService service;

    @Operation(summary = "Validar e consumir um código de pareamento",
            description = "Devolve o idosoId e o nome. Código inválido, expirado ou já usado responde 422.")
    @PostMapping("/validar")
    public PareamentoResponse validar(@Valid @RequestBody ValidarCodigoRequest request) {
        return service.validar(request.codigo());
    }
}
