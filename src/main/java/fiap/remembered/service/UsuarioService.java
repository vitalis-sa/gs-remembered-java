package fiap.remembered.service;

import fiap.remembered.dto.response.UsuarioResponse;
import fiap.remembered.entity.Usuario;
import fiap.remembered.mapper.UsuarioMapper;
import fiap.remembered.repository.AvisoRepository;
import fiap.remembered.repository.MedicamentoRepository;
import fiap.remembered.repository.RelatorioAdesaoRepository;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String RECURSO = "Idoso";

    private final UsuarioRepository usuarios;
    private final UsuarioMapper mapper;
    private final MedicamentoRepository medicamentos;
    private final AvisoRepository avisos;
    private final RelatorioAdesaoRepository relatorios;

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(UsuarioLogado logado) {
        return mapper.toResponse(usuarios.obter(logado.idosoId(), RECURSO));
    }

    @Transactional
    public void atualizarPushToken(UsuarioLogado logado, String pushToken) {
        usuarios.obter(logado.idosoId(), RECURSO).setPushToken(pushToken.trim());
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(UsuarioLogado logado, String nome) {
        Usuario usuario = usuarios.obter(logado.idosoId(), RECURSO);
        usuario.setNome(nome.trim());
        return mapper.toResponse(usuario);
    }

    /**
     * Direito de exclusao da LGPD: apaga a conta do idoso e os dados dele nesta API.
     * Avisos, relatorios e medicamentos apontam para TB_USUARIO sem ON DELETE CASCADE,
     * entao saem antes da conta. Vinculos e alertas da API .NET (tabelas NET_) nao sao tocados.
     */
    @Transactional
    public void excluirConta(UsuarioLogado logado) {
        Usuario usuario = usuarios.obter(logado.idosoId(), RECURSO);
        avisos.excluirDoIdoso(usuario.getId());
        relatorios.excluirDoIdoso(usuario.getId());
        medicamentos.excluirDoIdoso(usuario.getId());
        usuarios.delete(usuario);
    }
}
