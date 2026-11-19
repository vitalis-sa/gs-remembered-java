package fiap.remembered.service;

import fiap.remembered.dto.response.UsuarioResponse;
import fiap.remembered.entity.Usuario;
import fiap.remembered.enums.Perfil;
import fiap.remembered.mapper.UsuarioMapper;
import fiap.remembered.mapper.UsuarioMapperImpl;
import fiap.remembered.repository.AvisoRepository;
import fiap.remembered.repository.MedicamentoRepository;
import fiap.remembered.repository.RelatorioAdesaoRepository;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.security.UsuarioLogado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Long IDOSO_ID = 5L;

    @Mock UsuarioRepository usuarios;
    @Mock MedicamentoRepository medicamentos;
    @Mock AvisoRepository avisos;
    @Mock RelatorioAdesaoRepository relatorios;
    @Spy UsuarioMapper mapper = new UsuarioMapperImpl();
    @InjectMocks UsuarioService service;

    private final UsuarioLogado logado = new UsuarioLogado(String.valueOf(IDOSO_ID), Perfil.IDOSO, "Maria", Set.of());

    private Usuario maria() {
        Usuario usuario = new Usuario();
        usuario.setId(IDOSO_ID);
        usuario.setNome("Maria");
        usuario.setEmail("maria@teste.dev");
        return usuario;
    }

    @Test
    void atualizarPerfilTrocaONomeDoIdosoLogadoSemEspacosNasPontas() {
        Usuario usuario = maria();
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(usuario);

        UsuarioResponse resposta = service.atualizarPerfil(logado, "  Maria Souza  ");

        assertThat(usuario.getNome()).isEqualTo("Maria Souza");
        assertThat(resposta.nome()).isEqualTo("Maria Souza");
        assertThat(resposta.email()).isEqualTo("maria@teste.dev");
    }

    /**
     * TB_MEDICAMENTO, TB_AVISO e TB_RELATORIO_ADESAO apontam para TB_USUARIO sem ON DELETE CASCADE.
     * Apagar a conta antes deles viola a chave estrangeira (ORA-02292).
     */
    @Test
    void excluirContaApagaOsDadosDoIdosoAntesDaPropriaConta() {
        Usuario usuario = maria();
        when(usuarios.obter(IDOSO_ID, "Idoso")).thenReturn(usuario);

        service.excluirConta(logado);

        InOrder ordem = inOrder(avisos, relatorios, medicamentos, usuarios);
        ordem.verify(avisos).excluirDoIdoso(IDOSO_ID);
        ordem.verify(relatorios).excluirDoIdoso(IDOSO_ID);
        ordem.verify(medicamentos).excluirDoIdoso(IDOSO_ID);
        ordem.verify(usuarios).delete(usuario);
    }
}
