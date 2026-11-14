# Remembered — API Java (Spring Boot)

API da disciplina Java Advanced: medicamentos, doses, avisos, pareamento idoso/cuidador e relatório de adesão com IA (Groq via Spring AI).

## Requisitos
- Java 21, Oracle da FIAP e uma instancia no CloudAMQP (nada roda localmente alem da API)

## Como rodar
1. Copie `.env.example` para `.env` e preencha `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (min. 32 caracteres, igual ao da API .NET), `CLOUDAMQP_URL` e `GROQ_API_KEY`.
2. `.\run.ps1` (ou `.\mvnw.cmd spring-boot:run`).
3. Swagger: http://localhost:8080/swagger-ui.html

O Flyway executa um unico script (`V1__schema_inicial.sql`) com tabelas, indices, pacote `pkg_adesao` e trigger de auditoria.

## Deploy (Cloud Run)
Build pelo `Dockerfile` (a porta vem da variavel `PORT`). Configure as mesmas variaveis do `.env` como variaveis de ambiente/segredos do servico. Como a API tem jobs agendados e consumidor RabbitMQ, use `--no-cpu-throttling` e `--min-instances=1`; sem isso a CPU e cortada entre requisicoes e eles param.

## Testes
`.\mvnw.cmd test` (unitários, sem banco nem RabbitMQ).

## Autenticação
JWT HS256 com segredo e audience compartilhados com a API .NET. Papéis `IDOSO` e `CUIDADOR`; o token do cuidador traz a claim `idosos`. Acesso a recurso de outro idoso responde 404.

## Usuários de teste
| Papel | Como criar | Observação |
|---|---|---|
| IDOSO | `POST /api/v1/auth/cadastro` com `{"nome","email","senha"}` e depois `POST /api/v1/auth/login` | senha de 6 a 72 caracteres |
| CUIDADOR | cadastro na API .NET (mesmo banco e mesmo segredo JWT) | vincula-se ao idoso com o código de `POST /api/v1/me/codigo-pareamento` |

Usuários prontos para correção: **PREENCHER** (e-mail/senha do idoso e do cuidador de teste).

## Endpoints principais (`/api/v1`)
| Método e rota | Papel | Descrição |
|---|---|---|
| `POST /auth/cadastro`, `POST /auth/login` | público | cadastro e login do idoso |
| `GET /me`, `PUT /me/push-token`, `POST /me/codigo-pareamento` | autenticado | perfil, token de push e código de pareamento |
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
Link: **PREENCHER** (Cloud Run).

## Uso de IA generativa
O relatório de adesão usa o modelo `openai/gpt-oss-120b` no Groq. A IA só redige a partir de números já calculados e não dá orientação médica. O código foi desenvolvido com apoio do Claude.
