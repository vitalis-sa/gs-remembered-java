package fiap.remembered.controller;

import fiap.remembered.config.Rotas;
import fiap.remembered.dto.request.MedicamentoRequest;
import fiap.remembered.dto.response.MedicamentoResponse;
import fiap.remembered.security.AcessoAoIdoso;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Rotas.BASE)
@RequiredArgsConstructor
@Tag(name = "Medicamentos", description = "IDOSO (os próprios) e CUIDADOR (idosos vinculados)")
public class MedicamentoController {

    private final MedicamentoService service;

    @Operation(summary = "Listar medicamentos do idoso (paginado)")
    @GetMapping("/idosos/{idosoId}/medicamentos")
    @AcessoAoIdoso
    public Page<MedicamentoResponse> listar(@PathVariable Long idosoId,
                                            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(idosoId, pageable);
    }

    @Operation(summary = "Cadastrar medicamento",
            description = "Gera as doses dos próximos 7 dias e publica medicamento.criado. Se o cuidador cadastrar, o idoso recebe um aviso.")
    @PostMapping("/idosos/{idosoId}/medicamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @AcessoAoIdoso
    public MedicamentoResponse criar(@PathVariable Long idosoId,
                                     @Valid @RequestBody MedicamentoRequest request,
                                     @AuthenticationPrincipal UsuarioLogado logado) {
        return service.criar(idosoId, request, logado);
    }

    @Operation(summary = "Buscar medicamento por id")
    @GetMapping("/medicamentos/{id}")
    public MedicamentoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado logado) {
        return service.buscar(id, logado);
    }

    @Operation(summary = "Atualizar medicamento", description = "Regera as doses futuras ainda pendentes.")
    @PutMapping("/medicamentos/{id}")
    public MedicamentoResponse atualizar(@PathVariable Long id,
                                         @Valid @RequestBody MedicamentoRequest request,
                                         @AuthenticationPrincipal UsuarioLogado logado) {
        return service.atualizar(id, request, logado);
    }

    @Operation(summary = "Excluir medicamento", description = "Remove também horários e doses.")
    @DeleteMapping("/medicamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado logado) {
        service.excluir(id, logado);
    }
}
