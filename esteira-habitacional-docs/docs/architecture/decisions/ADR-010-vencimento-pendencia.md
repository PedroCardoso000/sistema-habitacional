# ADR-010 — Vencimento como condição calculada

## Status

Aceita

## Contexto

Persistir `VENCIDA` exigiria job e sincronização para representar uma condição que muda apenas com a passagem do tempo.

## Decisão

Persistir somente `OPEN`, `IN_PROGRESS`, `WAITING_RESPONSE`, `RESOLVED` e `CANCELLED`.

Uma pendência está vencida quando possui prazo anterior ao instante fornecido e não está resolvida nem cancelada. Domínio recebe `Instant` ou `Clock` explicitamente; não chama `Instant.now()` em regras testáveis.

Modelos de leitura podem expor `overdue: true` junto de `status` e `dueAt`. Filtros e índices devem consultar essa condição, sem gravar um sexto estado.

## Alternativas consideradas

- estado `OVERDUE`: rejeitado por exigir atualização periódica e permitir inconsistência.

## Consequências positivas

Não existe job apenas para sincronizar um estado derivado nem janela de inconsistência.

## Consequências negativas

Consultas dependentes de tempo precisam calcular a condição e possuir índices adequados.

## Riscos

Relógios, fusos ou comparações diferentes entre domínio e consulta podem divergir; instantes são tratados em UTC e testados na fronteira do prazo.

## Evidências e critérios de revisão

Revisar se vencimento passar a disparar uma transição de negócio independente e auditável.

## Data

2026-08-03
