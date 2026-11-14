package fiap.remembered.service;

import fiap.remembered.dto.response.UsuarioResponse;
import fiap.remembered.mapper.UsuarioMapper;
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

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(UsuarioLogado logado) {
        return mapper.toResponse(usuarios.obter(logado.idosoId(), RECURSO));
    }

    @Transactional
    public void atualizarPushToken(UsuarioLogado logado, String pushToken) {
        usuarios.obter(logado.idosoId(), RECURSO).setPushToken(pushToken.trim());
    }
}
