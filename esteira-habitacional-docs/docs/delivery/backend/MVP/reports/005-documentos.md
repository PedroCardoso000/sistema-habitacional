# Relatório de Entrega — Documentos e Submissão do Processo

## Resumo

Foi implementado o ciclo documental completo do MVP, com checklist inicial, solicitações, upload privado por
intenção temporária, versões imutáveis, análise, aprovação, recusa, reenvio e download protegido. A submissão do
rascunho agora valida cliente e imóvel, ativa o processo, inicializa o workflow, gera o checklist e define a próxima
ação em uma única transação.

## Fluxo implementado

```text
DocumentRequest/UploadIntent → casos de uso → autorização/portas → HTTP/JDBC/storage → PostgreSQL → testes
```

## Arquivos principais

### Domínio

- `DocumentType`, `ChecklistTemplate`, `DocumentRequest`, `DocumentVersion` e `UploadIntent`.
- Estados documentais e estados da intenção de upload.
- Eventos `DocumentRequested`, `DocumentSubmitted`, `DocumentApproved` e `DocumentRejected`.
- Transição explícita do `FinancingProcess` de rascunho para ativo somente quando cliente e imóvel existem.

### Aplicação

- `ProcessSubmissionService` coordena a submissão atômica.
- `DocumentService` coordena checklist, upload, confirmação, análise, versionamento, download e limpeza.
- Contratos intermodulares pequenos para ativação do processo, consulta de participantes, inicialização do workflow
  e definição da próxima ação.

### Adapters e infraestrutura

- `DocumentsController` expõe submissão, checklist, solicitações, upload, análise e download.
- `JdbcDocumentRepository` persiste catálogo, solicitações, versões, intenções e autorizações temporárias.
- `LocalPrivateDocumentStorage` mantém binários fora do banco e valida assinatura básica de PDF, JPEG e PNG.
- `DocumentCleanupJob` remove objetos órfãos expirados com repetição segura e log estruturado.
- Migration `V006__create_private_documents_and_submission.sql`.

## Regras protegidas

- Recusa exige motivo; reenvio adiciona versão e nunca altera a anterior.
- Extensão, MIME declarado, tamanho e assinatura básica do conteúdo são validados.
- Confirmação consulta tamanho, MIME e checksum no armazenamento privado.
- Confirmação duplicada não cria outra versão.
- Chaves de storage contêm apenas UUIDs, sem CPF, nome ou nome original do arquivo.
- Downloads usam autorização temporária de cinco minutos e uso único.
- Cliente e corretor somente acessam processo ao qual estão vinculados; terceiro e outra empresa são recusados.
- Gestor e analista dependem das permissões `VIEW_DOCUMENTS` e `MANAGE_DOCUMENTS`.
- Falha de storage mantém a intenção pendente; falha posterior deixa objeto elegível à limpeza.
- Falha no workflow ou checklist reverte ativação, auditoria, workflow, checklist e próxima ação.
- Toda consulta de banco relacionada ao agregado usa `organizationId` explícito e FKs compostas preservam o tenant.

## Testes executados

| Comando | Resultado |
|---|---|
| `.\mvnw.cmd test` | Sucesso: 52 testes unitários/arquiteturais |
| `.\mvnw.cmd test-compile '-Dit.test=DocumentsIT' failsafe:integration-test failsafe:verify` | Sucesso: 5 testes direcionados |
| `.\mvnw.cmd clean verify` | Sucesso: 52 testes unitários/arquiteturais e 26 testes de integração |
| Flyway/Testcontainers | V001 a V006 aplicadas em PostgreSQL 18 real |
| Checkstyle | 0 violações |
| `git diff --check` | Sucesso |

## Pendências ou riscos comprovados

- O adapter de binários do MVP usa filesystem privado local. Para múltiplas instâncias, produção deverá fornecer
  um adapter de storage privado compartilhado pela porta `PrivateDocumentStorage`.
- O vínculo do usuário externo com participante usa o mesmo UUID no contexto de identidade e no participante do
  processo. O provisionamento por IdP deverá preservar ou mapear explicitamente essa referência.
- O contexto autenticado por headers continua temporário, conforme a fundação do backend.

## Confirmação de escopo

Não foram implementadas notificações, OCR, antivírus externo, assinatura eletrônica, integração bancária,
storage de nuvem específico ou features de prompts posteriores.
