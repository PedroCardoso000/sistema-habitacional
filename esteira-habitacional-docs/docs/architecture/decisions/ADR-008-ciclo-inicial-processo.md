# ADR-008 — `RASCUNHO` antes de `EM_TRIAGEM`

## Status

Aceita

## Contexto

As especificações anteriores afirmavam simultaneamente que o estado inicial era `EM_TRIAGEM` e que `RASCUNHO` existia, sem explicar a transição.

## Decisão

- `CreateFinancingProcessDraft` cria o processo em `RASCUNHO`;
- `SubmitFinancingProcess` move um rascunho válido para `EM_TRIAGEM`;
- rascunho não possui workflow, não entra em fila ou SLA e não é visível para cliente/corretor;
- em triagem possui dados mínimos, responsável, workflow, checklist e próxima ação inicializados;
- os dois comportamentos permanecem casos de uso separados.

## Alternativas consideradas

- criação direta em triagem: rejeitada porque permite processo operacional parcialmente inicializado.
- terceiro estado técnico: rejeitado porque exporia falha de coordenação transacional como conceito de negócio.

## Consequências positivas

Nenhum processo entra na operação parcialmente inicializado.

## Consequências negativas

Criação e submissão exigem casos de uso, permissões e contratos distintos.

## Riscos

Rascunhos abandonados podem se acumular; retenção ou limpeza futura precisa respeitar a política ainda não decidida.

## Evidências e critérios de revisão

Revisar se o negócio eliminar formalmente rascunhos ou introduzir importação assíncrona.

## Data

2026-08-03
