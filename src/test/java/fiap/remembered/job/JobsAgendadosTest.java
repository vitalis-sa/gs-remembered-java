package fiap.remembered.job;

import fiap.remembered.service.DosesVencidasService;
import fiap.remembered.service.GeradorDeDoses;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * O banco da Azure e compartilhado entre a producao e a maquina de cada integrante.
 * Por isso os jobs precisam poder ser desligados no desenvolvimento local
 * (JOBS_HABILITADOS=false), deixando so a API da Azure processar as doses.
 */
class JobsAgendadosTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withBean(GeradorDeDoses.class, () -> mock(GeradorDeDoses.class))
            .withBean(DosesVencidasService.class, () -> mock(DosesVencidasService.class))
            .withUserConfiguration(AgendaDiariaJob.class, DosesVencidasJob.class);

    @Test
    void jobsSobemQuandoAPropriedadeNaoEInformada() {
        contexto.run(ctx -> {
            assertThat(ctx).hasSingleBean(AgendaDiariaJob.class);
            assertThat(ctx).hasSingleBean(DosesVencidasJob.class);
        });
    }

    @Test
    void jobsNaoSobemQuandoDesligadosNoDesenvolvimentoLocal() {
        contexto.withPropertyValues("remembered.jobs.habilitados=false").run(ctx -> {
            assertThat(ctx).doesNotHaveBean(AgendaDiariaJob.class);
            assertThat(ctx).doesNotHaveBean(DosesVencidasJob.class);
        });
    }
}
