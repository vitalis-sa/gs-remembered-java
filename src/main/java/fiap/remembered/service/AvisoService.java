package fiap.remembered.service;

import fiap.remembered.dto.response.AvisoResponse;
import fiap.remembered.entity.Aviso;
import fiap.remembered.exception.RecursoNaoEncontradoException;
import fiap.remembered.mapper.AvisoMapper;
import fiap.remembered.repository.AvisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvisoService {

    private final AvisoRepository avisos;
    private final AvisoMapper mapper;
    private final Clock clock;

    /** {@code lido} nulo traz todos; true so os lidos; false so os nao lidos. */
    @Transactional(readOnly = true)
    public Page<AvisoResponse> listar(Long idosoId, Boolean lido, Pageable pageable) {
        Page<Aviso> pagina = Optional.ofNullable(lido)
                .map(filtro -> filtro
                        ? avisos.findByIdosoIdAndLidoEmIsNotNull(idosoId, pageable)
                        : avisos.findByIdosoIdAndLidoEmIsNull(idosoId, pageable))
                .orElseGet(() -> avisos.findByIdosoId(idosoId, pageable));
        return pagina.map(mapper::toResponse);
    }

    @Transactional
    public AvisoResponse marcarComoLido(Long avisoId, Long idosoId) {
        Aviso aviso = avisos.findByIdAndIdosoId(avisoId, idosoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Aviso", avisoId));
        aviso.marcarComoLido(LocalDateTime.now(clock));
        return mapper.toResponse(aviso);
    }
}
