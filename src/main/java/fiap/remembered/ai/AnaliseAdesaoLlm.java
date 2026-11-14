package fiap.remembered.ai;

import java.util.List;

/** Formato estruturado que o LLM devolve; o Spring AI converte a resposta direto para este record. */
public record AnaliseAdesaoLlm(String resumo, List<String> pontosDeAtencao, String sugestaoParaFamilia) {
}
