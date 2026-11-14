package fiap.remembered.service;

import fiap.remembered.dto.request.MedicamentoRequest;
import fiap.remembered.dto.response.MedicamentoResponse;
import fiap.remembered.entity.Medicamento;
import fiap.remembered.enums.TipoCriador;
import fiap.remembered.exception.RequisicaoInvalidaException;
import fiap.remembered.mapper.MedicamentoMapper;
import fiap.remembered.messaging.evento.MedicamentoCriado;
import fiap.remembered.repository.MedicamentoRepository;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.security.VinculoGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private static final String RECURSO = "Medicamento";

    private final MedicamentoRepository medicamentos;
    private final UsuarioRepository usuarios;
    private final MedicamentoMapper mapper;
    private final GeradorDeDoses geradorDeDoses;
    private final VinculoGuard vinculoGuard;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public MedicamentoResponse criar(Long idosoId, MedicamentoRequest request, UsuarioLogado autor) {
        validarPeriodo(request);
        Medicamento medicamento = mapper.toEntity(request);
        medicamento.setIdoso(usuarios.obter(idosoId, "Idoso"));
        medicamento.setCriadoPorTipo(TipoCriador.de(autor.perfil(), request.origem()));
        medicamento.setCriadoPorNome(autor.nome());
        medicamento.substituirHorarios(mapper.toHorarios(request.horarios()));

        Medicamento salvo = medicamentos.save(medicamento);
        geradorDeDoses.agendar(salvo);
        eventos.publishEvent(MedicamentoCriado.de(salvo));
        return mapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public Page<MedicamentoResponse> listar(Long idosoId, Pageable pageable) {
        return medicamentos.findByIdosoId(idosoId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MedicamentoResponse buscar(Long id, UsuarioLogado logado) {
        return mapper.toResponse(obterAcessivel(id, logado));
    }

    @Transactional
    public MedicamentoResponse atualizar(Long id, MedicamentoRequest request, UsuarioLogado logado) {
        validarPeriodo(request);
        Medicamento medicamento = obterAcessivel(id, logado);
        mapper.atualizar(request, medicamento);
        medicamento.substituirHorarios(mapper.toHorarios(request.horarios()));
        geradorDeDoses.reagendar(medicamento);
        return mapper.toResponse(medicamento);
    }

    @Transactional
    public void excluir(Long id, UsuarioLogado logado) {
        medicamentos.delete(obterAcessivel(id, logado));
    }

    /** Medicamento achado por id: se nao for do idoso (ou de um idoso vinculado), responde como inexistente. */
    private Medicamento obterAcessivel(Long id, UsuarioLogado logado) {
        Medicamento medicamento = medicamentos.obter(id, RECURSO);
        vinculoGuard.exigirAcesso(logado, medicamento.getIdoso().getId(), RECURSO, id);
        return medicamento;
    }

    private void validarPeriodo(MedicamentoRequest request) {
        Optional.ofNullable(request.dataFim())
                .filter(fim -> fim.isBefore(request.dataInicio()))
                .ifPresent(fim -> {
                    throw new RequisicaoInvalidaException("A data de fim não pode ser anterior à data de início");
                });
    }
}
