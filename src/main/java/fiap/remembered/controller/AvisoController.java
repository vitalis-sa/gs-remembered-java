package fiap.remembered.controller;

import fiap.remembered.config.Rotas;
import fiap.remembered.dto.response.AvisoResponse;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.service.AvisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Rotas.BASE + "/avisos")
@RequiredArgsConstructor
@Tag(name = "Avisos", description = "Somente IDOSO")
public class AvisoController {

    private final AvisoService service;

    @Operation(summary = "Listar avisos (mais recentes primeiro)")
    @GetMapping
    public Page<AvisoResponse> listar(@AuthenticationPrincipal UsuarioLogado logado,
                                      @Parameter(description = "true: só lidos; false: só não lidos; omitido: todos")
                                      @RequestParam(required = false) Boolean lido,
                                      @ParameterObject @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(logado.idosoId(), lido, pageable);
    }

    @Operation(summary = "Marcar aviso como lido")
    @PatchMapping("/{id}/lido")
    public AvisoResponse marcarComoLido(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado logado) {
        return service.marcarComoLido(id, logado.idosoId());
    }
}
