package fiap.remembered.service;

import fiap.remembered.ai.AnaliseAdesaoLlm;
import fiap.remembered.ai.RelatorioLlmClient;
import fiap.remembered.dto.request.RelatorioAdesaoRequest;
import fiap.remembered.dto.response.DadosAdesao;
import fiap.remembered.dto.response.RelatorioAdesaoResponse;
import fiap.remembered.entity.RelatorioAdesao;
import fiap.remembered.repository.RelatorioAdesaoRepository;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.security.UsuarioLogado;
import fiap.remembered.security.VinculoGuard;
import fiap.remembered.util.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Relatorio de adesao: numeros do Oracle + texto do LLM. Este service NAO e transacional de
 * proposito: a chamada ao LLM pode levar ate o prazo configurado e nao deve segurar uma conexao
 * do banco enquanto espera.
 */
@Service
@RequiredArgsConstructor
public class RelatorioAdesaoService {

    private static final String RECURSO = "Relatório de adesão";
    private static final String AVISO_SEM_ANALISE =
            "O assistente de IA não respondeu a tempo; o relatório traz apenas os números calculados.";

    private final UsuarioRepository usuarios;
    private final RelatorioAdesaoRepository relatorios;
    private final AdesaoCalculadora calculadora;
    private final RelatorioLlmClient llm;
    private final VinculoGuard vinculoGuard;
    private final Json json;
    private final Clock clock;

    public RelatorioAdesaoResponse gerar(Long idosoId, RelatorioAdesaoRequest request) {
        var idoso = usuarios.obter(idosoId, "Idoso");
        LocalDate fim = LocalDate.now(clock);
        LocalDate inicio = fim.minusDays(request.diasOuPadrao() - 1L);

        DadosAdesao dados = calculadora.calcular(idosoId, inicio, fim);
        Optional<AnaliseAdesaoLlm> analise = llm.analisar(dados);

        RelatorioAdesao salvo = relatorios.save(RelatorioAdesao.builder()
                .idoso(idoso)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .percentualAdesao(dados.percentualGeral())
                .textoLlm(analise.map(json::escrever).orElse(null))
                .dadosJson(json.escrever(dados))
                .geradoEm(LocalDateTime.now(clock))
                .build());
        return paraResposta(salvo);
    }

    @Transactional(readOnly = true)
    public Page<RelatorioAdesaoResponse> listar(Long idosoId, Pageable pageable) {
        return relatorios.findByIdosoId(idosoId, pageable).map(this::paraResposta);
    }

    @Transactional(readOnly = true)
    public RelatorioAdesaoResponse buscar(Long id, UsuarioLogado logado) {
        RelatorioAdesao relatorio = relatorios.obter(id, RECURSO);
        vinculoGuard.exigirAcesso(logado, relatorio.getIdoso().getId(), RECURSO, id);
        return paraResposta(relatorio);
    }

    /** Unico caminho de montagem da resposta: o POST e o GET devolvem exatamente o que foi gravado. */
    private RelatorioAdesaoResponse paraResposta(RelatorioAdesao relatorio) {
        Optional<AnaliseAdesaoLlm> analise = json.lerOpcional(relatorio.getTextoLlm(), AnaliseAdesaoLlm.class);
        return new RelatorioAdesaoResponse(
                relatorio.getId(),
                relatorio.getIdoso().getId(),
                relatorio.getPeriodoInicio(),
                relatorio.getPeriodoFim(),
                relatorio.getPercentualAdesao(),
                json.ler(relatorio.getDadosJson(), DadosAdesao.class),
                analise.orElse(null),
                analise.isEmpty() ? AVISO_SEM_ANALISE : null,
                relatorio.getGeradoEm());
    }
}
