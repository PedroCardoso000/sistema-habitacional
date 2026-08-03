# Relatório de Entrega — Processo de Financiamento

## Resumo

Foram implementados rascunhos de processos de financiamento originados por corretor ou cliente direto. Gestores e analistas podem criar, editar, consultar e listar rascunhos, associar participantes, definir cliente principal, alterar prioridade e associar ou substituir imóvel com preservação do histórico.

## Fluxo implementado

```text
FinancingProcess → casos de uso → autorização/portas → HTTP/JDBC → PostgreSQL → testes
```

## Arquivos alterados

### Domínio

- Agregado `FinancingProcess` e tipos de origem, status, prioridade, participante e associação de imóvel.
- Invariantes de origem, autoria, corretor vinculado, status inicial, versão e histórico de imóveis.
- Evento `FinancingProcessDraftCreated` para consumo futuro pela linha do tempo.

### Aplicação

- Portas de gestão e consulta de processo.
- Validação de referências de cliente e corretor por contratos públicos do módulo `parties`.
- Autorização interna, auditoria, paginação, filtros e controle otimista de concorrência.

### Adapters e infraestrutura

- API tenant-scoped em `/api/organizations/{organizationId}/processes`.
- Repositório, gerador de número sequencial por empresa e auditoria JDBC.
- Migration `V004__create_financing_process_drafts.sql` com constraints, índices e FKs compostas por empresa.

### Testes

- Domínio: criação direta, origem por corretor, imóvel opcional, histórico e vínculo do corretor.
- Integração/API: duas origens, concorrência otimista, isolamento multiempresa, invisibilidade ao corretor, broker ausente ou de outro tenant e migration real.

## Regras protegidas

- Todo processo nasce em `DRAFT`, com prioridade `NORMAL`, autor e responsável interno autenticados.
- Origem `BROKER` exige corretor ativo da mesma empresa; origem `DIRECT_CLIENT` não aceita corretor.
- Cliente principal e imóvel são opcionais durante o rascunho.
- Cada imóvel associado gera entrada imutável e sequencial no histórico.
- Número do processo é único por empresa.
- Toda leitura e escrita usa `organizationId`; o banco também protege tabelas filhas por FK composta.
- Alterações exigem `expectedVersion` e usam `UPDATE` condicionado à versão persistida.
- Somente gestores e analistas acessam rascunhos; corretor e cliente permanecem sem visibilidade.
- Não há workflow, checklist, SLA, fila operacional nem decisão bancária nesta entrega.

## Segurança e auditoria

A API valida contexto, vínculo ativo, papel e permissão. Ações relevantes registram empresa, processo, ator, ação, instante e correlation ID. O evento de criação contém apenas identificadores operacionais e não representa decisão oficial de instituição financeira.

## Testes executados

| Comando | Resultado |
|---|---|
| `.\\mvnw.cmd -Dtest=FinancingProcessTest,LayerDependencyTest,ModularityTest test` | Sucesso: 7 testes |
| `.\\mvnw.cmd -Dit.test=FinancingProcessIT failsafe:integration-test failsafe:verify` | Sucesso: 4 testes de integração com PostgreSQL 18 |
| `.\\mvnw.cmd clean verify` | Sucesso: 33 testes unitários/arquiteturais e 17 testes de integração |
| Flyway/Testcontainers | V001 a V004 aplicadas em PostgreSQL real |
| Checkstyle | 0 violações na validação intermediária |

## Pendências ou riscos comprovados

- O contexto por headers continua sendo mecanismo temporário de desenvolvimento, conforme Prompt 001.
- O evento de criação está publicado, mas a timeline imutável pertence ao Prompt 007.
- Workflow e entrada operacional pertencem aos Prompts 004 e 005; rascunhos deliberadamente não aparecem em filas.

## Confirmação de escopo

Não foram implementados workflow, submissão, documentos, pendências, SLA, filas, integração bancária ou decisão de crédito. O Prompt 004 não foi antecipado.
