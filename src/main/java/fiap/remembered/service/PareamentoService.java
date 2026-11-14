package fiap.remembered.service;

import fiap.remembered.config.PareamentoProperties;
import fiap.remembered.dto.response.CodigoPareamentoResponse;
import fiap.remembered.dto.response.PareamentoResponse;
import fiap.remembered.entity.Usuario;
import fiap.remembered.exception.CodigoPareamentoInvalidoException;
import fiap.remembered.repository.UsuarioRepository;
import fiap.remembered.util.CodigoAleatorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * O idoso gera o codigo e o passa ao cuidador: gerar o codigo e o ato de consentimento. O codigo e
 * de uso unico e expira.
 */
@Service
@RequiredArgsConstructor
public class PareamentoService {

    private static final int TAMANHO_DO_CODIGO = 6;

    private final UsuarioRepository usuarios;
    private final PareamentoProperties propriedades;
    private final Clock clock;

    @Transactional
    public CodigoPareamentoResponse gerarCodigo(Long idosoId) {
        Usuario idoso = usuarios.obter(idosoId, "Idoso");
        String codigo = Stream.generate(() -> CodigoAleatorio.gerar(TAMANHO_DO_CODIGO))
                .filter(candidato -> !usuarios.existsByCodigoPareamento(candidato))
                .findFirst()
                .orElseThrow();
        LocalDateTime expiraEm = LocalDateTime.now(clock).plusMinutes(propriedades.validadeMinutos());
        idoso.definirCodigoPareamento(codigo, expiraEm);
        return new CodigoPareamentoResponse(codigo, expiraEm);
    }

    /** Confere e consome o codigo. O consumo e atomico (um UPDATE): nenhum codigo vale para dois cuidadores. */
    @Transactional
    public PareamentoResponse validar(String codigoInformado) {
        String codigo = codigoInformado.trim().toUpperCase(Locale.ROOT);
        Usuario idoso = usuarios.findByCodigoPareamento(codigo).orElseThrow(CodigoPareamentoInvalidoException::new);
        Optional.of(usuarios.consumirCodigo(idoso.getId(), codigo, LocalDateTime.now(clock)))
                .filter(consumidos -> consumidos == 1)
                .orElseThrow(CodigoPareamentoInvalidoException::new);
        return new PareamentoResponse(idoso.getId(), idoso.getNome());
    }
}
