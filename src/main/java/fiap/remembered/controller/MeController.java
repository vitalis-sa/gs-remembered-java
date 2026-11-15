package fiap.remembered.controller;

import fiap.remembered.config.Rotas;
import fiap.remembered.dto.request.AtualizarPerfilRequest;
import fiap.remembered.dto.request.PushTokenRequest;
import fiap.remembered.dto.response.CodigoPareamentoResponse;
import fiap.remembered.dto.response.UsuarioResponse;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.service.PareamentoService;
import fiap.remembered.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Rotas.BASE + "/me")
@RequiredArgsConstructor
@Tag(name = "Minha conta", description = "Somente IDOSO")
public class MeController {

    private final UsuarioService usuarioService;
    private final PareamentoService pareamentoService;

    @Operation(summary = "Dados do idoso logado")
    @GetMapping
    public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioLogado logado) {
        return usuarioService.perfil(logado);
    }

    @Operation(summary = "Atualizar o nome do idoso logado")
    @PutMapping
    public UsuarioResponse atualizarPerfil(@AuthenticationPrincipal UsuarioLogado logado,
                                           @Valid @RequestBody AtualizarPerfilRequest request) {
        return usuarioService.atualizarPerfil(logado, request.nome());
    }

    @Operation(summary = "Excluir a conta do idoso logado",
            description = "Direito de exclusao (LGPD): apaga a conta, os medicamentos (com horarios e doses), os avisos e os relatorios do idoso.")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirConta(@AuthenticationPrincipal UsuarioLogado logado) {
        usuarioService.excluirConta(logado);
    }

    @Operation(summary = "Registrar o token de push do aparelho (Expo)")
    @PutMapping("/push-token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarPushToken(@AuthenticationPrincipal UsuarioLogado logado,
                                   @Valid @RequestBody PushTokenRequest request) {
        usuarioService.atualizarPushToken(logado, request.pushToken());
    }

    @Operation(summary = "Gerar código de pareamento",
            description = "O idoso passa o código ao cuidador; gerar o código é o registro do consentimento. Uso único, expira em 15 min.")
    @PostMapping("/codigo-pareamento")
    public CodigoPareamentoResponse gerarCodigo(@AuthenticationPrincipal UsuarioLogado logado) {
        return pareamentoService.gerarCodigo(logado.idosoId());
    }
}
