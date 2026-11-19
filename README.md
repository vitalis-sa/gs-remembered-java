# Remembered API (serviço do idoso, Java)

Um app para o idoso nunca esquecer um remédio e para a família acompanhar de perto: lembretes na hora certa, confirmação com um toque, aviso ao cuidador quando uma dose é pulada e um relatório de adesão escrito por inteligência artificial.

Projeto entregue para a disciplina **Java Advanced** (FIAP, Global Solution 2026/2, tema **CitizenTech**).
Este repositório é a **API do idoso**: medicamentos, doses, avisos, pareamento e relatório de adesão com IA. A **API .NET** cuida do cuidador (cadastro, login, vínculos, alertas e consultas), e as duas conversam por um segredo JWT compartilhado e por uma fila RabbitMQ. Veja a [arquitetura](#arquitetura).

## Entrega

| Artefato | Link |
|---|---|
| Repositório GitHub (Java) | https://github.com/vitalis-sa/gs-remembered-java |
| Repositório GitHub (.NET) | _PREENCHER_ |
| Repositório GitHub (aplicativo mobile) | _PREENCHER_ |
| Vídeo pitch (até 5 min) | [Assistir no YouTube](https://youtu.be/kPCyNc_gmdU) |
| Aplicação online: API Java | https://rm566234-gs-api.azurewebsites.net |
| Swagger da API Java no ar | https://rm566234-gs-api.azurewebsites.net/swagger-ui.html |
| Saúde da API Java | https://rm566234-gs-api.azurewebsites.net/actuator/health |
| Aplicação online: API .NET (cuidador) | https://rm566234-gs-dotnet.azurewebsites.net/api/v1 |
| Coleção Postman para testar o fluxo | [`docs/Remembered_GS.postman_collection.json`](docs/Remembered_GS.postman_collection.json) (ver [Postman](#postman-fluxo-para-testar)) |

## Grupo

| Nome | RM |
|---|---|
| Ana Flávia Camelo | RM561489 |
| Gustavo Kenji Terada | RM562745 |
| João Guilherme Carvalho Novaes | RM566234 |
| Pedro Chasci Puga | RM565154 |
| Lucas Figueiredo Vieira | RM561342 |

## O problema e a solução (CitizenTech)

Quem toma remédios todo dia, em especial o idoso, esquece doses, e quem cuida dele só descobre tarde. O Remembered ataca isso nos dois lados:

- o **idoso** recebe o lembrete, confirma com um toque e cadastra ou consulta o que toma;
- o **cuidador** (familiar ou profissional) é avisado na hora de uma dose pulada, acompanha a adesão e pede um relatório escrito por IA.

A IA **só redige** a partir de números já calculados pela API e nunca dá orientação médica. Alinha-se aos ODS 3 (saúde e bem-estar), 10 (redução das desigualdades) e 11 (cidades e comunidades sustentáveis).

## Tecnologias

| Tecnologia | Descrição |
|---|---|
| Java 21 | Linguagem do projeto, com records, `Optional` em pipeline, `switch` de expressão e text blocks |
| Spring Boot 4.1.2 | Base da API REST, com Tomcat embutido e configuração por propriedades |
| Maven (wrapper 3.9.15) | Build e dependências; o `mvnw` dispensa instalar o Maven |
| Spring Data JPA + Hibernate 7 | Acesso ao Oracle; `ddl-auto=validate` confere as entidades com as tabelas |
| Oracle Database (Azure) | Banco compartilhado das duas APIs: tabelas `TB_` são do Java e `NET_` do .NET |
| Flyway 12 | Schema versionado em `db/migration`; histórico próprio em `FLYWAY_HISTORY_REMEMBERED` para não colidir com outros projetos do mesmo schema |
| PL/SQL | Pacote `pkg_adesao` (cálculo de adesão) e gatilho de auditoria de medicamentos |
| Spring Security + JJWT 0.12.6 | JWT HS256 stateless, papéis `IDOSO` e `CUIDADOR`, BCrypt nas senhas |
| Spring AI 2.0.1 + Groq | `ChatClient` apontado para a API do Groq (compatível com a da OpenAI), modelo `openai/gpt-oss-120b` |
| Spring AMQP + RabbitMQ (CloudAMQP) | Eventos assíncronos entre as APIs, com ack manual, retentativas e fila de erros |
| Bean Validation | Validação dos DTOs, com erro `400` campo a campo |
| MapStruct 1.6.3 e Lombok | Conversão entidade/DTO e menos código repetitivo |
| Springdoc OpenAPI 3.0.3 | Swagger UI e contrato OpenAPI |
| Spring Scheduling | Rotinas que geram a agenda de doses e marcam as vencidas |
| Spring Boot Actuator | `/actuator/health` para monitorar a API |

## Arquitetura

Opção escolhida no requisito 5 do PDF: **A) RabbitMQ**, com eventos publicados e consumidos de forma assíncrona.

```mermaid
flowchart LR
    App[Aplicativo Expo<br/>idoso e cuidador]
    Java[Remembered Java<br/>idoso, doses e IA]
    Net[Remembered .NET<br/>cuidador e alertas]
    Rabbit[(RabbitMQ<br/>CloudAMQP)]
    Oracle[(Oracle<br/>schema REMEMBERED)]
    Groq[Groq<br/>gpt-oss-120b]
    Expo[Push do Expo]

    App -->|login e dados do idoso| Java
    App -->|login e dados do cuidador| Net
    Net -->|repassa o token do cuidador| Java
    Java -->|publica dose.pulada| Rabbit
    Net -->|publica consulta.criada| Rabbit
    Rabbit -->|consome| Net
    Rabbit -->|consome| Java
    Java --> Oracle
    Net --> Oracle
    Java -->|relatório de adesão| Groq
    Java --> Expo
    Net --> Expo
```

| Serviço | Tecnologia | É dono de | Onde roda |
|---|---|---|---|
| **remembered-java** (este repositório) | Java 21, Spring Boot | Idoso, medicamentos, doses, avisos, pareamento e relatório de adesão com IA | Azure Web App `rm566234-gs-api` |
| **remembered-dotnet** | .NET, Minimal API | Cuidador (cadastro e login), vínculos, alertas de dose pulada, consultas e locais | Azure Web App `rm566234-gs-dotnet` |
| **Aplicativo** | Expo / React Native | Interface do idoso e do cuidador | Fala com os dois serviços |

### Como os serviços conversam

| Fluxo | Mecanismo | Detalhe |
|---|---|---|
| Autenticação | JWT HS256 com segredo e audience compartilhados | A Java emite o token do idoso e a .NET emite o do cuidador. A Java aceita os dois emissores (`remembered-java` e `remembered-dotnet`) |
| Cuidador usa a API Java | Mesmo token da .NET | A claim `idosos` lista os idosos vinculados; ela decide o que o cuidador enxerga |
| Vínculo idoso e cuidador | Código de pareamento | O idoso gera o código na Java; o cuidador o informa à .NET, que o confere em `POST /pareamento/validar` na Java |
| Relatório pedido pelo cuidador | .NET repassa à Java | O LLM roda só na Java |
| Eventos | Fila RabbitMQ | Veja [Arquitetura distribuída](#arquitetura-distribuída-rabbitmq) |

## Instalação

Pré-requisitos:

- **Java 21** (`java -version`)
- Acesso ao **Oracle** do grupo na Azure e à instância do **CloudAMQP**. Os valores são passados pelo grupo por canal privado e nunca vão para o Git.
- Uma chave do **Groq** (https://console.groq.com/keys). Sem ela a API sobe e o relatório sai só com os números.

```bash
git clone https://github.com/vitalis-sa/gs-remembered-java.git
cd gs-remembered-java
cp .env.example .env     # no Windows: copy .env.example .env
```

Preencha o `.env` (o arquivo é ignorado pelo Git):

| Variável | Para quê |
|---|---|
| `DB_URL` | `jdbc:oracle:thin:@//<host>:1521/FREEPDB1` |
| `DB_USER`, `DB_PASSWORD` | Usuário e senha do schema `REMEMBERED` (compartilhado pelas duas APIs) |
| `JWT_SECRET` | Segredo do JWT, no mínimo 32 caracteres, **igual ao da API .NET** |
| `CLOUDAMQP_URL` | URL AMQP do painel do CloudAMQP (`amqps://usuario:senha@host/vhost`) |
| `GROQ_API_KEY` | Chave do Groq |
| `JOBS_HABILITADOS`, `RABBIT_CONSUMIDORES` | Deixe `false` na sua máquina (veja o aviso abaixo) |

> **Atenção:** o banco e a fila são os da produção. Na sua máquina mantenha `JOBS_HABILITADOS=false` e `RABBIT_CONSUMIDORES=false`, para que só a API da Azure gere a agenda, marque as doses vencidas e consuma as filas.

## Execução

```powershell
.\run.ps1
```

O script carrega o `.env`, avisa se faltar alguma variável obrigatória e chama o Maven. Também funciona rodar direto, porque a classe `ConfiguracaoDotenv` lê o `.env` da raiz no boot:

```bash
./mvnw spring-boot:run
```

Para empacotar e rodar o jar:

```bash
./mvnw clean package
java -jar target/remembered-0.0.1-SNAPSHOT.jar
```

Ao subir, a aplicação:

1. aplica as migrations do Flyway (`src/main/resources/db/migration`; uma migration já aplicada nunca é editada, a mudança entra numa nova `V4__...`);
2. valida se as entidades JPA batem com as tabelas (`ddl-auto=validate`);
3. sobe em `http://localhost:8080`.

| Recurso | URL |
|---|---|
| API REST | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Saúde | `http://localhost:8080/actuator/health` |

## Acesso à aplicação

A API Java é consumida pelo aplicativo móvel e pela API .NET. Há **duas formas de entrar**:

| Quem | Como entra | Papel no token |
|---|---|---|
| Idoso | `POST /api/v1/auth/cadastro` e `POST /api/v1/auth/login` na **API Java** | `IDOSO` |
| Cuidador | `POST /auth/cadastro` e `POST /auth/login` na **API .NET** | `CUIDADOR`, com a claim `idosos` |

**Toda rota fora de `/api/v1/auth/**` exige autenticação.** Sem token a resposta é `401`.

### Usuários de teste

Dados fictícios, criados pela migration `V3__dados_teste.sql`:

| Papel | E-mail | Senha |
|---|---|---|
| IDOSO | `maria.idosa@teste.dev` | `Remembered@2026` |
| IDOSO | `jose.idoso@teste.dev` | `Remembered@2026` |
| CUIDADOR | crie pela API .NET (cadastro) ou rode a [coleção Postman](#postman-fluxo-para-testar), que cria um cuidador novo a cada execução | senha de 6 a 72 caracteres |

Maria tem Losartana (crítico, 08:00 e 20:00) e Metformina (12:00). José tem Sinvastatina (21:00).

### 1. Cadastrar o idoso

```bash
curl -X POST https://rm566234-gs-api.azurewebsites.net/api/v1/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Teste","email":"maria.teste@teste.dev","senha":"senha123"}'
```

O cadastro já devolve o token, dispensando um login em seguida. A senha é gravada com hash BCrypt e nunca volta em nenhuma resposta.

### 2. Autenticar

```bash
curl -X POST https://rm566234-gs-api.azurewebsites.net/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.idosa@teste.dev","senha":"Remembered@2026"}'
```

Resposta (`200`):

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-11-20T10:00:00",
  "usuario": { "id": 1, "nome": "Maria Aparecida", "email": "maria.idosa@teste.dev", "criadoEm": "2026-11-14T15:00:00" }
}
```

Credencial errada devolve `401` com a mesma mensagem para senha errada e e-mail inexistente, para não revelar quais e-mails existem.

### 3. Chamar a API com o token

O token vale **24 horas** (`remembered.jwt.validade=PT24H`) e vai no cabeçalho `Authorization`:

```bash
curl https://rm566234-gs-api.azurewebsites.net/api/v1/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 4. Pelo Swagger UI

1. Abra `/swagger-ui.html`
2. Em **Autenticação**, execute `POST /api/v1/auth/login` e copie o valor de `token`
3. Clique em **Authorize** (canto superior direito) e cole **só o token**, sem escrever `Bearer`
4. A partir daí todo *Try it out* já envia o cabeçalho sozinho

### O que cada resposta de erro significa

Todos os erros saem no padrão **RFC 9457** (`ProblemDetail`), com `status`, `title`, `detail`, `path` e, na validação, a lista `errors` por campo.

| Status | Significado | O que fazer |
|---|---|---|
| `400` | Falha de validação ou corpo mal formado | A resposta lista o erro campo a campo |
| `401` | Sem token, token expirado, assinatura inválida ou credencial errada | Refaça o login |
| `403` | Autenticado, mas o papel (ou o id da rota) não tem permissão | Use o papel certo, ou o id de um idoso vinculado |
| `404` | Recurso inexistente **ou** de outro idoso | Confira o id; ninguém enxerga dado alheio |
| `409` | Regra de negócio recusou: e-mail em uso, dose já registrada, violação de integridade | Leia o `detail` |
| `422` | Código de pareamento inválido ou expirado | Gere outro código |
| `500` | Erro inesperado | Mensagem genérica; o detalhe fica só no log |

## Postman: fluxo para testar

A coleção [`docs/Remembered_GS.postman_collection.json`](docs/Remembered_GS.postman_collection.json) percorre o fluxo de negócio inteiro, nos **dois serviços publicados**, e pode ser executada de uma vez (*Run collection*). Cada execução cria um idoso e um cuidador novos, então pode ser repetida. Os scripts guardam tokens e ids em variáveis da coleção (`javaUrl` e `dotnetUrl` já apontam para a Azure) e cada requisição tem testes que conferem o status esperado.

Para importar: **Postman → Import →** selecione o arquivo → abra a coleção → **Run**.

| Pasta | O que demonstra |
|---|---|
| 0. Saúde | A API Java está no ar (`UP`) |
| 1. Idoso (Java) | Cadastro, login com JWT, perfil e token de push |
| 2. Medicamentos | CRUD com Bean Validation e lista paginada; ao salvar, a agenda de 7 dias é gerada |
| 3. Doses | Filtro por data e status, confirmação ("Tomei") e recusa de confirmar duas vezes (`409`) |
| 4. Pareamento | O idoso gera o código de uso único |
| 5. Cuidador (.NET) | Cadastro, login, vínculo com o código e novo login para o token ganhar a claim `idosos` |
| 6. Cuidador na Java | O token da .NET vale na Java: lista, cadastra, acompanha doses e gera o relatório com IA (pela .NET e direto) |
| 7. Avisos | O aviso do cuidador chega ao idoso pela fila |
| 8. Segurança | `401`, `403`, `404`, `400`, `409`, `422` esperados |
| 9. Limpeza | Remove o medicamento e exclui a conta do idoso (LGPD) |

> A pasta 5 segue as rotas do Plano Técnico da API .NET (`/auth/cadastro`, `/auth/login`, `/vinculos`). Se o Swagger da .NET tiver outro formato de corpo, ajuste só essa pasta.

## Testes automatizados

```bash
./mvnw test
```

Testes unitários (JUnit 5, Mockito e AssertJ), sem banco nem RabbitMQ:

| Teste | O que cobre |
|---|---|
| `JwtServiceTest` | Emissão do token do idoso, leitura do token do cuidador, issuer e claim `idosos` |
| `VinculoGuardTest` | Quem enxerga qual idoso (`403` na rota, `404` no recurso) |
| `GeradorDeDosesTest` | Agenda de 7 dias, dias da semana (posição 0 = segunda) e regeração |
| `AdesaoTest` | Cálculo do percentual de adesão |
| `RegistroDoseTest` | Ciclo da dose: só PENDENTE é confirmável |
| `GlobalExceptionHandlerTest` | Status e mensagem de cada tipo de erro |
| `RelatorioLlmClientTest` | O relatório sai só com os números quando o LLM falha ou demora |
| `JobsAgendadosTest` | Jobs de agenda e de doses vencidas |
| `UsuarioServiceTest` | Cadastro, atualização e exclusão da conta |
| `TipoCriadorTest`, `TentativasTest` | Quem criou o medicamento e retentativas com espera crescente |
| `DocumentacaoOpenApiTest` | O Swagger lê as descrições dos campos dos DTOs (compatibilidade das bibliotecas de documentação) |
| `RememberedApplicationTests` | Fuso único da aplicação (America/Sao_Paulo) |

## Perfis e permissões

| | `IDOSO` | `CUIDADOR` |
|---|---|---|
| Como entra | Java (`/auth/cadastro` e `/auth/login`) | .NET, com a claim `idosos` |
| Conta (`/me`) e avisos (`/avisos`) | sim | não |
| Código de pareamento | gera | só valida, pela .NET |
| Medicamentos e doses | os próprios | dos idosos vinculados |
| Relatório de adesão (IA) | não | dos idosos vinculados |

O papel é checado por rota no `SecurityConfig` (`hasRole`), e o **vínculo** por recurso em `VinculoGuard`: o idoso acessa só o próprio id e o cuidador só os ids da claim `idosos`. Na rota com o id do idoso, negar vira `403`; ao buscar um recurso por id (medicamento, dose, relatório), negar vira `404`, igual a "não existe", para não confirmar que o id existe.

## Fluxos completos (não-CRUD)

### 1. Doses: agenda, confirmação e dose pulada

| Passo | Regra |
|---|---|
| Geração | Ao cadastrar ou editar um medicamento, `GeradorDeDoses` cria as doses dos próximos **7 dias**, respeitando os dias da semana de cada horário. Um job logo após a meia-noite completa a agenda |
| Confirmação | `POST /doses/{id}/confirmar` leva a dose de `PENDENTE` para `TOMADA`; confirmar de novo devolve `409` |
| Dose pulada | O job `DosesVencidasJob` roda a cada minuto e marca como `PULADA` a dose `PENDENTE` há mais de **30 minutos** |
| Adesão | `TOMADA / (TOMADA + PULADA)`, calculada pelo pacote Oracle `pkg_adesao` |
| Alerta ao cuidador | Dose pulada publica `dose.pulada`: severidade `ALTA` se o medicamento é crítico; `MEDIA` se ele pulou o `limitePulos` (padrão 3) em 7 dias |

### 2. Relatório de adesão com IA (Spring AI)

`POST /idosos/{id}/relatorios-adesao` (cuidador vinculado): a API calcula os números do período (`AdesaoCalculadora`), entrega ao Groq por `RelatorioLlmClient` e grava o resultado em `TB_RELATORIO_ADESAO`. Detalhes na seção [Inteligência artificial](#inteligência-artificial-groq-e-spring-ai).

### 3. Avisos ao idoso (assíncrono)

Quando o **cuidador** cadastra um medicamento ou marca uma consulta, o idoso recebe um push e um item em `TB_AVISO`. O cadastro feito pelo próprio idoso não gera aviso. Veja a [arquitetura distribuída](#arquitetura-distribuída-rabbitmq).

## Endpoints

Todos sob `/api/v1`. Legenda da coluna **Acesso**: `público` dispensa token; `idoso` e `cuidador` exigem o papel; `ambos` aceita os dois (o cuidador só nos idosos vinculados).

### Autenticação

| Método e rota | Acesso | Descrição |
|---|---|---|
| `POST /auth/cadastro` | público | Cadastra o idoso e devolve o token |
| `POST /auth/login` | público | Login do idoso |

### Conta do idoso

| Método e rota | Acesso | Descrição |
|---|---|---|
| `GET /me` | idoso | Perfil |
| `PUT /me` | idoso | Troca o nome |
| `DELETE /me` | idoso | Exclui a conta com todos os dados (LGPD) |
| `PUT /me/push-token` | idoso | Registra o token de push do aparelho |
| `POST /me/codigo-pareamento` | idoso | Gera o código de pareamento (uso único, 15 min) |

### Pareamento

| Método e rota | Acesso | Descrição |
|---|---|---|
| `POST /pareamento/validar` | cuidador | Confere e consome o código; devolve `idosoId` e `nome` |

### Medicamentos

| Método e rota | Acesso | Descrição |
|---|---|---|
| `GET /idosos/{idosoId}/medicamentos` | ambos | Lista paginada (`page`, `size`, `sort`) |
| `POST /idosos/{idosoId}/medicamentos` | ambos | Cadastra, com horários, e gera as doses |
| `GET /medicamentos/{id}` | ambos | Busca por id |
| `PUT /medicamentos/{id}` | ambos | Atualiza e regera as doses futuras pendentes |
| `DELETE /medicamentos/{id}` | ambos | Remove, com horários e doses |

### Doses

| Método e rota | Acesso | Descrição |
|---|---|---|
| `GET /idosos/{idosoId}/doses?data=&status=` | ambos | Doses do dia (hoje por padrão), com filtro de status |
| `POST /doses/{id}/confirmar` | ambos | Marca a dose como tomada |

### Avisos

| Método e rota | Acesso | Descrição |
|---|---|---|
| `GET /avisos?lido=` | idoso | Avisos paginados, mais recentes primeiro |
| `PATCH /avisos/{id}/lido` | idoso | Marca como lido |

### Relatório de adesão (IA)

| Método e rota | Acesso | Descrição |
|---|---|---|
| `POST /idosos/{idosoId}/relatorios-adesao` | cuidador | Gera o relatório (corpo opcional `{"dias": 7}`, de 1 a 90) |
| `GET /idosos/{idosoId}/relatorios-adesao` | cuidador | Lista paginada |
| `GET /relatorios-adesao/{id}` | cuidador | Busca por id |

Exemplo de corpo para cadastrar um medicamento:

```json
{
  "nome": "Losartana",
  "dosagem": "50 mg",
  "forma": "Comprimido",
  "critico": true,
  "limitePulos": 3,
  "dataInicio": "2026-11-19",
  "horarios": [
    { "hora": "08:00", "diasSemana": "1111111" },
    { "hora": "20:00" }
  ]
}
```

`diasSemana` tem sete dígitos 0/1, da segunda ao domingo (posição 0 = segunda); sem ele, vale todos os dias. A documentação completa e interativa está no Swagger.

## Inteligência artificial (Groq e Spring AI)

| Item | Valor |
|---|---|
| Funcionalidade | Relatório de adesão: resume a adesão geral e a variação em relação à semana anterior, aponta os medicamentos ou horários mais pulados e sugere um apoio prático à família. Dúvida clínica é encaminhada ao médico |
| Modelo | `openai/gpt-oss-120b` no Groq, pela API compatível com a da OpenAI |
| Integração | Spring AI, `ChatClient` em `RelatorioLlmClient`; o prompt fica em `src/main/resources/prompts/relatorio-adesao-system.st` |
| Entrada | Só números já calculados (`DadosAdesao`): nada de texto livre do usuário |
| Saída | JSON estruturado (`AnaliseAdesaoLlm`), gravado junto do relatório |
| Segurança | A IA **só redige**: não dá orientação médica, não sugere dose nem troca de remédio |
| Falha | Timeout de 15 s e tratamento de erro: se o Groq não responde, o relatório sai só com os números e um campo `aviso`. Sem chave, a API sobe normalmente |
| Custo | O relatório fica salvo no banco e é reaproveitado, não regenerado a cada abertura |

## Arquitetura distribuída (RabbitMQ)

Exchange `remembered.eventos` (tipo topic) no CloudAMQP.

| Evento (routing key) | Quem publica | Quem consome | O que acontece |
|---|---|---|---|
| `dose.pulada` | Java (job de doses vencidas) | .NET | Cria um alerta por cuidador vinculado e envia o push |
| `medicamento.criado` | Java (todo cadastro de medicamento) | Java | Se quem cadastrou foi o cuidador, cria o aviso ao idoso e envia o push; cadastro do próprio idoso não gera aviso |
| `consulta.criada` | .NET | Java | Cria o aviso ao idoso e envia o push |

Como funciona na Java:

- O evento é publicado **depois do commit** da transação (`EventoPublisher`), então nunca anuncia algo que não foi gravado.
- O consumidor (`AvisoNovidadeListener`) usa fila `idoso.aviso-novidade` com fila de erros (DLQ), **ack manual** e **3 tentativas** com espera crescente.
- Idempotência por `evento_id`: a mesma mensagem entregue duas vezes não duplica o aviso.
- Se o broker estiver fora do ar, a API continua respondendo: a publicação falha no log e o dado fica gravado.

## Banco de dados

O Oracle é compartilhado pelas duas APIs, no schema `REMEMBERED` (serviço `FREEPDB1`). As tabelas `TB_` são do Java e as `NET_` do .NET, e cada API só cria as próprias.

| Migration | Conteúdo |
|---|---|
| `V1__schema_inicial.sql` | Tabelas `TB_USUARIO`, `TB_MEDICAMENTO`, `TB_HORARIO_MEDICAMENTO`, `TB_REGISTRO_DOSE`, `TB_AVISO`, `TB_RELATORIO_ADESAO` e `TB_AUDITORIA_MEDICAMENTO`, índices, pacote `pkg_adesao` e gatilho `trg_auditoria_medicamento` |
| `V2__corrige_trigger_auditoria.sql` | Recria o gatilho de auditoria de forma válida |
| `V3__dados_teste.sql` | Idosos e medicamentos fictícios, gravados só se ainda não existirem |

Relacionamentos: um **usuário (idoso)** tem vários **medicamentos**; cada medicamento tem vários **horários** e vários **registros de dose**; o idoso recebe **avisos** e tem **relatórios de adesão**. Mudança de tabela entra sempre como migration nova, nunca por `ALTER` manual, porque o schema é compartilhado com a produção.

## Deploy (Azure)

| Item | Valor |
|---|---|
| API Java | Azure Web App `rm566234-gs-api` (Java SE 21, região eastus): https://rm566234-gs-api.azurewebsites.net |
| API .NET | Azure Web App `rm566234-gs-dotnet`: https://rm566234-gs-dotnet.azurewebsites.net/api/v1 |
| Banco | Oracle Free em container no Azure Container Instances, schema `REMEMBERED` |
| Fila | CloudAMQP, a mesma instância das duas APIs |
| Pipeline | CI/CD da disciplina de DevOps (Azure DevOps): recebe cada versão deste repositório por pull request, compila, roda os testes e publica no Web App |

As variáveis do `.env` viram *app settings* do Web App, e os segredos ficam no *variable group* do Azure DevOps. O `Dockerfile` continua disponível para rodar a API em container (ele lê a porta de `PORT`), mas não é usado no deploy da Azure. Em produção, `JOBS_HABILITADOS` e `RABBIT_CONSUMIDORES` ficam `true`.

## Estrutura do projeto

```
fiap.remembered
├── RememberedApplication.java   → Classe principal (fuso único America/Sao_Paulo)
│
├── config/                      → Segurança, OpenAPI, propriedades, .env, rotas
│   ├── SecurityConfig.java      → Regras de acesso por rota e papel
│   ├── Rotas.java               → Mapa único das rotas (controllers e segurança usam as mesmas constantes)
│   └── ConfiguracaoDotenv.java  → Carrega o .env como fonte de configuração
│
├── security/                    → JwtService, filtro JWT, UsuarioLogado e VinculoGuard
├── controller/                  → Auth, Me, Medicamento, Dose, Aviso, Pareamento, RelatorioAdesao
├── service/                     → Regras de negócio (doses, adesão, pareamento, relatório)
├── repository/                  → Spring Data JPA
├── entity/                      → Usuario, Medicamento, HorarioMedicamento, RegistroDose, Aviso, RelatorioAdesao
├── dto/                         → Records de request e response, com Bean Validation
├── mapper/                      → MapStruct
├── ai/                          → RelatorioLlmClient (Spring AI) e AnaliseAdesaoLlm
├── messaging/                   → RabbitConfig, EventoPublisher, AvisoNovidadeListener e os eventos
├── job/                         → AgendaDiariaJob e DosesVencidasJob
├── integration/                 → PushService (push do Expo)
├── exception/                   → GlobalExceptionHandler e as exceções de domínio
├── enums/ e util/               → Enums de domínio e utilitários (Adesao, Tentativas, Json...)
│
src/main/resources
├── application.properties       → Configuração, sempre por variável de ambiente
├── db/migration/                → V1, V2 e V3 do Flyway
└── prompts/                     → Prompt do relatório de adesão
```

## Requisitos técnicos atendidos

Checklist da **Global Solution 2026/2, Java Advanced**:

| # | Requisito do PDF | Pts | Como é atendido | Onde ver |
|---|---|---|---|---|
| 1 | API REST com 3+ entidades relacionadas, CRUD JPA e paginação | 20 | 6 entidades relacionadas; CRUD de medicamento (com horários); `Page<T>` em medicamentos, avisos e relatórios | `entity/`, `MedicamentoController`, [Banco de dados](#banco-de-dados) |
| 2 | Bean Validation e tratamento global | 10 | `@Valid` nos DTOs e `@RestControllerAdvice` com respostas RFC 9457 | `GlobalExceptionHandler`, [Erros](#o-que-cada-resposta-de-erro-significa) |
| 3 | Spring Security + JWT com 2 papéis | 20 | `IDOSO` e `CUIDADOR`, rotas por papel e vínculo por recurso | `SecurityConfig`, `JwtService`, [Perfis](#perfis-e-permissões) |
| 4 | Spring AI integrado ao fluxo | 20 | Relatório de adesão com Groq, não é chat solto | `RelatorioLlmClient`, [IA](#inteligência-artificial-groq-e-spring-ai) |
| 5 | Arquitetura distribuída (A ou B) | 20 | **Opção A**: RabbitMQ no CloudAMQP, evento publicado e consumido de forma assíncrona | `messaging/`, [RabbitMQ](#arquitetura-distribuída-rabbitmq) |
| 6 | Swagger, README e API consumida pelo app | 10 | Swagger no ar, este README, coleção Postman; o app Expo consome a API | [Entrega](#entrega), [Postman](#postman-fluxo-para-testar) |

Outros pontos: Flyway com schema versionado, PL/SQL no Oracle (`pkg_adesao` e gatilho de auditoria), `/actuator/health`, segredos só por variável de ambiente e exclusão de conta para a LGPD.

## Uso de IA generativa

- **Na solução:** o relatório de adesão usa o modelo `openai/gpt-oss-120b` no Groq, pelo Spring AI. A IA só redige a partir de números já calculados e não dá orientação médica.
- **No desenvolvimento:** o código e a documentação foram escritos com apoio do **Claude (Anthropic)**, e o grupo revisou, testou e ajustou o resultado.
