package fiap.remembered.controller;

import fiap.remembered.config.Rotas;
import fiap.remembered.dto.request.RelatorioAdesaoRequest;
import fiap.remembered.dto.response.RelatorioAdesaoResponse;
import fiap.remembered.security.AcessoAoIdoso;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.service.RelatorioAdesaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(Rotas.BASE)
@RequiredArgsConstructor
@Tag(name = "Relatórios de adesão", description = "Somente CUIDADOR vinculado (chamado pela API .NET)")
public class RelatorioAdesaoController {

    private final RelatorioAdesaoService service;

    @Operation(summary = "Gerar relatório de adesão",
            description = "Números calculados no Oracle + texto do LLM (Spring AI/Groq). Se o LLM falhar ou passar de 15 s, "
                    + "o relatório é salvo só com os números e a resposta traz o campo aviso.")
    @PostMapping("/idosos/{idosoId}/relatorios-adesao")
    @ResponseStatus(HttpStatus.CREATED)
    @AcessoAoIdoso
    public RelatorioAdesaoResponse gerar(@PathVariable Long idosoId,
                                         @Valid @RequestBody(required = false) RelatorioAdesaoRequest request) {
        return service.gerar(idosoId, Optional.ofNullable(request).orElseGet(RelatorioAdesaoRequest::padrao));
    }

    @Operation(summary = "Listar relatórios do idoso (mais recentes primeiro)")
    @GetMapping("/idosos/{idosoId}/relatorios-adesao")
    @AcessoAoIdoso
    public Page<RelatorioAdesaoResponse> listar(@PathVariable Long idosoId,
                                                @ParameterObject @PageableDefault(size = 20, sort = "geradoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(idosoId, pageable);
    }

    @Operation(summary = "Buscar relatório por id")
    @GetMapping("/relatorios-adesao/{id}")
    public RelatorioAdesaoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado logado) {
        return service.buscar(id, logado);
    }
}
