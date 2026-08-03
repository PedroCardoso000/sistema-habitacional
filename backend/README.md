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

## Rascunhos de financiamento

Endpoints internos para gestores e analistas:

- `POST /api/organizations/{organizationId}/processes`
- `GET /api/organizations/{organizationId}/processes`
- `GET /api/organizations/{organizationId}/processes/{processId}`
- `PATCH /api/organizations/{organizationId}/processes/{processId}/main-client`
- `PUT /api/organizations/{organizationId}/processes/{processId}/participants`
- `PUT /api/organizations/{organizationId}/processes/{processId}/property`
- `PATCH /api/organizations/{organizationId}/processes/{processId}/priority`

Alterações recebem `expectedVersion` e retornam `409` quando o rascunho foi modificado por outra operação. O imóvel é opcional e cada substituição preserva o histórico. Rascunhos não possuem workflow, etapa, próxima ação ou visibilidade para corretor e cliente.

## Workflow operacional

Endpoints internos:

- `PUT /api/organizations/{organizationId}/workflow/models/initial`
- `GET /api/organizations/{organizationId}/processes/{processId}/workflow`
- `PATCH /api/organizations/{organizationId}/processes/{processId}/workflow/advance`
- `PATCH /api/organizations/{organizationId}/processes/{processId}/workflow/return`
- `PATCH /api/organizations/{organizationId}/processes/{processId}/workflow/exception`
- `PATCH /api/organizations/{organizationId}/processes/{processId}/workflow/block`
- `PATCH /api/organizations/{organizationId}/processes/{processId}/workflow/unblock`
- `PUT /api/organizations/{organizationId}/processes/{processId}/workflow/next-action`

O fluxo inicial v1 possui seis etapas ordenadas. Avanços respeitam critérios obrigatórios configurados; retornos e saltos exigem justificativa e permissão elevada de gestor. Mudanças preservam histórico imutável e usam `expectedVersion`. Não existe endpoint para inicializar uma jornada: o contrato interno `InitializeWorkflowForSubmissionUseCase` somente aceita processo `ACTIVE` e será coordenado pela submissão atômica do Prompt 005.

## Documentos e submissão

Endpoints principais:

- `POST /api/organizations/{organizationId}/processes/{processId}/submission`
- `GET /api/organizations/{organizationId}/processes/{processId}/documents`
- `POST /api/organizations/{organizationId}/processes/{processId}/document-requests`
- `POST /api/organizations/{organizationId}/document-requests/{requestId}/uploads`
- `POST /api/organizations/{organizationId}/uploads/{uploadId}/complete`
- `PATCH /api/organizations/{organizationId}/document-requests/{requestId}/review`
- `PATCH /api/organizations/{organizationId}/document-requests/{requestId}/approval`
- `PATCH /api/organizations/{organizationId}/document-requests/{requestId}/rejection`
- `PATCH /api/organizations/{organizationId}/document-requests/{requestId}/resubmission`
- `POST /api/organizations/{organizationId}/document-versions/{versionId}/downloads`

A submissão exige cliente principal e imóvel, ativa o rascunho, inicializa o workflow em `INITIAL_REVIEW`,
gera o checklist e define a próxima ação na mesma transação. Arquivos são enviados por intenção temporária,
confirmados contra metadados do storage privado e baixados por autorização temporária de uso único. Chaves
de objeto usam apenas UUIDs. O job de limpeza remove intenções e objetos órfãos expirados de forma idempotente.

O storage local privado usa `DOCUMENTS_STORAGE_ROOT`; sem configuração, grava em diretório privado dentro do
temporário do sistema operacional. Produção deve apontar essa propriedade para volume privado persistente.

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
| `DOCUMENTS_STORAGE_ROOT` | diretório privado no temporário do sistema |

Credenciais padrão existem somente para desenvolvimento local. Ambientes reais devem injetar segredos externamente.

## Limites atuais

Não existem autenticação federada real, submissão pública, notificações ou integração com storage de nuvem.
O adapter atual usa filesystem privado configurável e URLs internas temporárias; ambientes com múltiplas instâncias
precisarão substituir essa porta por armazenamento privado compartilhado. O Event Publication Registry garante
entrega aos listeners transacionais registrados; não é event store nem timeline.
