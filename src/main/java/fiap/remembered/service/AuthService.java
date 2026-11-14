package fiap.remembered.service;

import fiap.remembered.dto.request.CadastroRequest;
import fiap.remembered.dto.request.LoginRequest;
import fiap.remembered.dto.response.AuthResponse;
import fiap.remembered.entity.Usuario;
import fiap.remembered.exception.EmailJaCadastradoException;
import fiap.remembered.mapper.UsuarioMapper;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.security.JwtService;
import fiap.remembered.security.TokenEmitido;
import fiap.remembered.util.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper mapper;

    @Transactional
    public AuthResponse cadastrar(CadastroRequest request) {
        String email = Textos.emailNormalizado(request.email());
        if (usuarios.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        Usuario idoso = usuarios.save(Usuario.novo(request.nome().trim(), email, passwordEncoder.encode(request.senha())));
        return respostaComToken(idoso);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        return usuarios.findByEmail(Textos.emailNormalizado(request.email()))
                .filter(idoso -> passwordEncoder.matches(request.senha(), idoso.getSenhaHash()))
                .map(this::respostaComToken)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
    }

    private AuthResponse respostaComToken(Usuario idoso) {
        TokenEmitido token = jwtService.emitir(idoso);
        return AuthResponse.bearer(token.valor(), token.expiraEm(), mapper.toResponse(idoso));
    }
}
