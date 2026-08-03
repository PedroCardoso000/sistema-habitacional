# Relatório de Entrega — Fluxo, Etapas e Próxima Ação

## Resumo

Foi implementada a jornada operacional versionada do financiamento. O modelo inicial v1 reúne as seis etapas do MVP em ordem única. Processos ativos podem avançar, retornar, saltar por exceção autorizada, bloquear ou desbloquear a etapa atual e receber próxima ação com responsável interno e prazo opcional. Rascunhos continuam sem workflow.

## Fluxo implementado

```text
WorkflowModel/WorkflowJourney → casos de uso → autorização/portas → HTTP/JDBC → PostgreSQL → testes
```

## Arquivos alterados

### Domínio

- `WorkflowModel` e `WorkflowStageDefinition` versionados.
- `WorkflowJourney`, `ProcessStage`, `StageTransition`, `NextAction` e seus estados.
- Regras de avanço, bloqueio, retorno, exceção justificada e limpeza de próxima ação obsoleta.
- Eventos `WorkflowInitialized`, `ProcessStageChanged` e `NextActionDefined`.

### Aplicação

- Configuração idempotente do fluxo inicial.
- Contrato interno `InitializeWorkflowForSubmissionUseCase`, sem endpoint público.
- Gestão e consulta da jornada com concorrência otimista.
- Contratos tenant-scoped para referência de processo e responsável interno ativo.

### Adapters e infraestrutura

- API aninhada em `/api/organizations/{organizationId}/processes/{processId}/workflow`.
- Repositório e auditoria JDBC.
- Migration `V005__create_versioned_workflow.sql` com modelos, definições, jornadas, etapas, transições e auditoria.

### Testes

- Domínio: avanço válido, critério ausente, bloqueio, retorno, exceção sem justificativa, responsável ausente, histórico e rejeição de rascunho.
- Aplicação: coordenação de inicialização e transição com persistência, auditoria e eventos.
- Integração/API: modelo v1, lifecycle, próxima ação, avanço, retorno, bloqueio, versão, permissão elevada e isolamento multiempresa.

## Regras protegidas

- Rascunho não recebe workflow.
- Jornada sempre referencia versão imutável do modelo usado na inicialização.
- Avanço normal é apenas para a próxima etapa e exige todos os critérios obrigatórios configurados.
- Etapa bloqueada não avança.
- Retorno e salto exigem justificativa e permissão `AUTHORIZE_WORKFLOW_EXCEPTION`, concedida somente ao gestor.
- Toda mudança preserva transição anterior; tabelas históricas não são atualizadas nem removidas.
- Mudança de etapa remove próxima ação anterior para impedir instrução obsoleta.
- Próxima ação exige responsável interno ativo da mesma empresa.
- Queries, FKs e índices mantêm `organizationId` explícito.
- Operações usam versão otimista no domínio da aplicação e no `UPDATE` do banco.
- Processos `ACTIVE` não podem mais ser alterados pelos casos de uso de edição de rascunho.

## Segurança e auditoria

Gestores e analistas podem operar transições comuns. Somente gestores autorizam retorno ou salto. A inicialização é uma porta interna, valida processo ativo e ator interno, e não pode ser chamada pela API HTTP. Alterações registram empresa, processo, ator, ação, instante e correlation ID.

## Testes executados

| Comando | Resultado |
|---|---|
| `.\\mvnw.cmd -Dtest=WorkflowJourneyTest,WorkflowServiceTest test` | Sucesso: 8 testes |
| `.\\mvnw.cmd -Dit.test=WorkflowIT verify` | Sucesso: 41 testes unitários/arquiteturais e 4 testes de integração direcionados |
| `.\\mvnw.cmd clean verify` | Sucesso: 41 testes unitários/arquiteturais e 21 testes de integração |
| Flyway/Testcontainers | V001 a V005 aplicadas em PostgreSQL 18 real |
| Checkstyle | 0 violações nas validações intermediárias |

## Pendências ou riscos comprovados

- Os critérios documentais concretos ainda não existem na especificação; o modelo suporta critérios obrigatórios, mas o fluxo inicial v1 não inventa requisitos antes do checklist do Prompt 005.
- A alteração `DRAFT → ACTIVE` e a coordenação atômica com `InitializeWorkflowForSubmissionUseCase` pertencem ao Prompt 005.
- O contexto por headers continua temporário, conforme Prompt 001.

## Confirmação de escopo

Não foram implementados submissão pública, checklist, documentos, pendências, SLA, filas ou integração bancária. O Prompt 005 não foi antecipado.
