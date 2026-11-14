package fiap.remembered.controller;

import fiap.remembered.config.Rotas;
import fiap.remembered.dto.response.DoseResponse;
import fiap.remembered.enums.StatusDose;
import fiap.remembered.security.AcessoAoIdoso;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.service.DoseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(Rotas.BASE)
@RequiredArgsConstructor
@Tag(name = "Doses", description = "IDOSO (as próprias) e CUIDADOR (idosos vinculados)")
public class DoseController {

    private final DoseService service;

    @Operation(summary = "Doses do dia", description = "Sem data, traz as de hoje. Filtro opcional por status.")
    @GetMapping("/idosos/{idosoId}/doses")
    @AcessoAoIdoso
    public List<DoseResponse> listar(@PathVariable Long idosoId,
                                     @Parameter(description = "Dia (AAAA-MM-DD). Padrão: hoje.")
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                     @RequestParam(required = false) StatusDose status) {
        return service.listar(idosoId, data, status);
    }

    @Operation(summary = "Confirmar dose (\"Tomei\")",
            description = "Só doses PENDENTES podem ser confirmadas; TOMADA e PULADA não voltam atrás (409).")
    @PostMapping("/doses/{id}/confirmar")
    public DoseResponse confirmar(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado logado) {
        return service.confirmar(id, logado);
    }
}
