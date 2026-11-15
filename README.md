# Remembered — API Java (Spring Boot)

API da disciplina Java Advanced: medicamentos, doses, avisos, pareamento idoso/cuidador e relatório de adesão com IA (Groq via Spring AI).

## Requisitos
- Java 21, o Oracle compartilhado do grupo na Azure (container no ACI) e a instância do CloudAMQP. Nada roda localmente além da API.

## Como rodar
1. Copie `.env.example` para `.env` e preencha `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (min. 32 caracteres, igual ao da API .NET), `CLOUDAMQP_URL` e `GROQ_API_KEY`. Os valores são passados pelo grupo por canal privado e nunca vão para o Git.
   - O banco da Azure é o mesmo da produção. Na sua máquina, mantenha `JOBS_HABILITADOS=false` e `RABBIT_CONSUMIDORES=false`, para que só a API da Azure gere a agenda, marque as doses vencidas e consuma as filas.
2. `.\run.ps1` (ou `.\mvnw.cmd spring-boot:run`).
3. Swagger: http://localhost:8080/swagger-ui.html

O Flyway cria o schema na partida: `V1__schema_inicial.sql` (tabelas, índices, pacote `pkg_adesao` e trigger de auditoria), `V2__corrige_trigger_auditoria.sql` e `V3__dados_teste.sql` (usuários e medicamentos fictícios). Mudança de tabela entra sempre como uma migration nova, nunca por ALTER manual, porque o schema é compartilhado com a produção.

## Deploy (Azure)
A produção roda no Azure Web App `rm566234-gs-api` (Java SE 21, região eastus), com o Oracle Free em container no Azure Container Instances. O deploy é feito pelo pipeline de CI/CD da disciplina de DevOps (Azure DevOps), que recebe cada versão deste repositório por pull request. As variáveis do `.env` viram app settings do Web App, e os segredos ficam no variable group do Azure DevOps. O `Dockerfile` continua disponível para rodar a API em container, mas não é usado no deploy da Azure.

## Testes
`.\mvnw.cmd test` (unitários, sem banco nem RabbitMQ).

## Autenticação
JWT HS256 com segredo e audience compartilhados com a API .NET. Papéis `IDOSO` e `CUIDADOR`; o token do cuidador traz a claim `idosos`. Acesso a recurso de outro idoso responde 404.

## Usuários de teste
| Papel | Como criar | Observação |
|---|---|---|
| IDOSO | `POST /api/v1/auth/cadastro` com `{"nome","email","senha"}` e depois `POST /api/v1/auth/login` | senha de 6 a 72 caracteres |
| CUIDADOR | cadastro na API .NET (mesmo banco e mesmo segredo JWT) | vincula-se ao idoso com o código de `POST /api/v1/me/codigo-pareamento` |

Usuários prontos para correção (dados fictícios, criados pela migration V3):

| Papel | E-mail | Senha |
|---|---|---|
| IDOSO | `maria.idosa@teste.dev` | `Remembered@2026` |
| IDOSO | `jose.idoso@teste.dev` | `Remembered@2026` |

O cuidador de teste é cadastrado na API .NET.

## Endpoints principais (`/api/v1`)
| Método e rota | Papel | Descrição |
|---|---|---|
| `POST /auth/cadastro`, `POST /auth/login` | público | cadastro e login do idoso |
| `GET /me`, `PUT /me`, `DELETE /me` | idoso | perfil, troca do nome e exclusão da conta com os dados (LGPD) |
| `PUT /me/push-token`, `POST /me/codigo-pareamento` | idoso | token de push e código de pareamento |
| `POST /pareamento/validar` | cuidador | vincula o cuidador ao idoso pelo código |
| `GET/POST /idosos/{id}/medicamentos` | idoso ou cuidador vinculado | lista paginada e cadastro |
| `GET/PUT/DELETE /medicamentos/{id}` | idoso ou cuidador vinculado | leitura, edição e remoção |
| `GET /idosos/{id}/doses` | idoso ou cuidador vinculado | doses por período |
| `POST /doses/{id}/confirmar` | idoso ou cuidador vinculado | marca a dose como tomada |
| `GET /avisos`, `PATCH /avisos/{id}/lido` | autenticado | avisos paginados |
| `POST/GET /idosos/{id}/relatorios-adesao` | cuidador | gera (Spring AI) e lista relatórios |

Documentação completa e interativa no Swagger. Erros seguem o padrão RFC 9457 (`ProblemDetail`).

## Arquitetura distribuída: opção A (RabbitMQ)
Exchange `remembered.eventos` (topic) no CloudAMQP, com os eventos `dose.pulada`, `medicamento.criado` e `consulta.criada`. A API publica depois do commit da transação e um consumidor assíncrono cria o aviso e envia o push. Ack manual, 3 tentativas com espera crescente, fila de erros (DLQ) e idempotência por `evento_id`.

## Link do deploy
https://rm566234-gs-api.azurewebsites.net (Swagger em `/swagger-ui.html`)

## Uso de IA generativa
O relatório de adesão usa o modelo `openai/gpt-oss-120b` no Groq. A IA só redige a partir de números já calculados e não dá orientação médica. O código foi desenvolvido com apoio do Claude.
