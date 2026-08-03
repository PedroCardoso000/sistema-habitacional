# Backend — Esteira Habitacional

Backend modular da Esteira Habitacional, implementado a partir das especificações de entrega do MVP.

## Requisitos

- Java 25 LTS em uma distribuição OpenJDK compatível;
- Docker com suporte a Compose e Testcontainers;
- nenhum Maven global é necessário.

## Validação completa

No Windows:

```powershell
.\mvnw.cmd clean verify
```

Em Linux ou macOS:

```bash
./mvnw clean verify
```

Esse é o comando oficial e executa compilação, testes unitários, testes de integração com PostgreSQL real, verificação de migrations, Spring Modulith, ArchUnit, Checkstyle e empacotamento.

## Execução local

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

Endpoints técnicos:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /v3/api-docs`

## Contexto autenticado temporário

Durante o desenvolvimento, os endpoints funcionais recebem o contexto técnico pelos headers:

- `X-User-Id`: UUID do usuário;
- `X-Organization-Id`: UUID da empresa do vínculo ativo.

Esses headers não concedem acesso por si mesmos. O backend recupera o usuário com escopo explícito de empresa e revalida status, papel e permissão. O adapter está isolado para futura substituição por um provedor de identidade real.

Endpoints da entrega de identidade e autorização:

- `GET /api/identity/context`
- `POST /api/organizations/{organizationId}/users`
- `PATCH /api/organizations/{organizationId}/users/{userId}/role`
- `POST /api/organizations/{organizationId}/users/{userId}/suspension`
- `POST /api/organizations/{organizationId}/users/{userId}/revocation`
- `POST /api/platform/organizations`

Endpoints de participantes e parceiros:

- `POST /api/organizations/{organizationId}/parties/clients`
- `POST /api/organizations/{organizationId}/parties/clients/search`
- `POST /api/organizations/{organizationId}/parties/brokers`
- `POST /api/organizations/{organizationId}/parties/agencies`
- `PUT /api/organizations/{organizationId}/parties/brokers/{brokerId}/agency`
- `PATCH /api/organizations/{organizationId}/parties/{type}/{partyId}/contact`
- `PUT /api/organizations/{organizationId}/parties/partners/{type}/{partnerId}/status`
- `GET /api/organizations/{organizationId}/parties?type=CLIENT&page=0&size=20`

Os valores de `type` são `CLIENT`, `BROKER` ou `AGENCY`; para alteração de status são aceitos `BROKER` e `AGENCY`. CPF e CNPJ são dados restritos usados apenas no cadastro, busca autorizada e prevenção de duplicidade. Eles não são retornados nas respostas nem gravados nas trilhas de auditoria.

O contrato detalhado, incluindo requests, responses e erros RFC 9457, é gerado em `GET /v3/api-docs`.

## Bootstrap inicial

O provisionamento da primeira empresa e do administrador da plataforma não é endpoint HTTP. Para executá-lo uma única vez, configure as variáveis abaixo e inicie a aplicação:

```text
BOOTSTRAP_ENABLED=true
BOOTSTRAP_EXECUTE=true
BOOTSTRAP_EXPECTED_SECRET=<segredo-configurado-no-ambiente>
BOOTSTRAP_SUPPLIED_SECRET=<segredo-fornecido-ao-comando>
BOOTSTRAP_ORGANIZATION_NAME=<nome-da-primeira-empresa>
BOOTSTRAP_ADMINISTRATOR_EMAIL=<email-do-administrador>
BOOTSTRAP_ADMINISTRATOR_DISPLAY_NAME=<nome-do-administrador>
```

Após o sucesso, desabilite `BOOTSTRAP_EXECUTE` e remova os segredos do ambiente. O banco impede repetição do provisionamento e execuções concorrentes são serializadas por lock transacional.

## Configuração

| Variável | Padrão local |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/esteira_habitacional` |
| `DATABASE_USERNAME` | `esteira` |
| `DATABASE_PASSWORD` | `esteira` |
| `PLATFORM_ORGANIZATION_CREATION_ENABLED` | `false` |

Credenciais padrão existem somente para desenvolvimento local. Ambientes reais devem injetar segredos externamente.

## Limites atuais

Não existem autenticação federada real, entidades de processo, storage de documentos ou notificações. Clientes, corretores e imobiliárias pertencem exclusivamente à empresa que os cadastrou; ainda não existem vínculos com processos. O Event Publication Registry garante entrega aos listeners transacionais registrados; não é event store nem timeline.
