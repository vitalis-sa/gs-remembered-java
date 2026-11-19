package fiap.remembered.service;

import fiap.remembered.config.JwtProperties;
import fiap.remembered.dto.request.CadastroRequest;
import fiap.remembered.dto.request.LoginRequest;
import fiap.remembered.dto.response.AuthResponse;
import fiap.remembered.entity.Usuario;
import fiap.remembered.exception.EmailJaCadastradoException;
import fiap.remembered.mapper.UsuarioMapperImpl;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.security.JwtService;
import fiap.remembered.util.Fuso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres!!";
    private static final String SENHA = "senha123";

    @Mock UsuarioRepository usuarios;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private JwtService jwtService;
    private AuthService service;

    @BeforeEach
    void preparar() {
        var propriedades = new JwtProperties(SEGREDO, "remembered", "remembered-java",
                List.of("remembered-java"), Duration.ofHours(24));
        jwtService = new JwtService(propriedades, Clock.system(Fuso.ZONA));
        service = new AuthService(usuarios, encoder, jwtService, new UsuarioMapperImpl());
    }

    private Usuario maria() {
        Usuario usuario = Usuario.novo("Maria", "maria@teste.dev", encoder.encode(SENHA));
        usuario.setId(10L);
        return usuario;
    }

    @Test
    void cadastrarNormalizaOEmailGravaASenhaComHashEDevolveUmTokenValido() {
        when(usuarios.existsByEmail("maria@teste.dev")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(chamada -> {
            Usuario salvo = chamada.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        AuthResponse resposta = service.cadastrar(new CadastroRequest("  Maria  ", "  MARIA@Teste.dev ", SENHA));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario gravado = captor.getValue();
        assertThat(gravado.getNome()).isEqualTo("Maria");
        assertThat(gravado.getEmail()).isEqualTo("maria@teste.dev");
        assertThat(gravado.getSenhaHash()).isNotEqualTo(SENHA);
        assertThat(encoder.matches(SENHA, gravado.getSenhaHash())).isTrue();

        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.usuario().id()).isEqualTo(10L);
        assertThat(jwtService.interpretar(resposta.token()))
                .hasValueSatisfying(logado -> assertThat(logado.idosoId()).isEqualTo(10L));
    }

    @Test
    void cadastrarComEmailJaUsadoRecusaESemGravarNada() {
        when(usuarios.existsByEmail("maria@teste.dev")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(new CadastroRequest("Maria", "Maria@Teste.dev", SENHA)))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void loginComAsCredenciaisCertasDevolveUmTokenDoIdoso() {
        when(usuarios.findByEmail("maria@teste.dev")).thenReturn(Optional.of(maria()));

        AuthResponse resposta = service.login(new LoginRequest(" MARIA@teste.dev ", SENHA));

        assertThat(resposta.usuario().email()).isEqualTo("maria@teste.dev");
        assertThat(jwtService.interpretar(resposta.token()))
                .hasValueSatisfying(logado -> assertThat(logado.idosoId()).isEqualTo(10L));
    }

    /** Senha errada e e-mail inexistente respondem igual: dizer qual falhou revelaria quais e-mails existem. */
    @Test
    void loginComSenhaErradaEComEmailInexistenteFalhamComAMesmaMensagem() {
        when(usuarios.findByEmail("maria@teste.dev")).thenReturn(Optional.of(maria()));
        when(usuarios.findByEmail("ninguem@teste.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("maria@teste.dev", "senha-errada")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");
        assertThatThrownBy(() -> service.login(new LoginRequest("ninguem@teste.dev", SENHA)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");
    }
}
