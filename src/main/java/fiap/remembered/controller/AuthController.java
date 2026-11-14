package fiap.remembered.controller;

import fiap.remembered.config.Rotas;
import fiap.remembered.dto.request.CadastroRequest;
import fiap.remembered.dto.request.LoginRequest;
import fiap.remembered.dto.response.AuthResponse;
import fiap.remembered.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Rotas.BASE + "/auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Autenticação", description = "Cadastro e login do idoso")
public class AuthController {

    private final AuthService service;

    @Operation(summary = "Cadastrar idoso", description = "Cria a conta e já devolve um token JWT (válido por 24 h).")
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse cadastrar(@Valid @RequestBody CadastroRequest request) {
        return service.cadastrar(request);
    }

    @Operation(summary = "Login do idoso")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
