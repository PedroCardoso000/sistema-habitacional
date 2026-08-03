# ADR-009 — Submissão e inicialização atômicas

## Status

Aceita

## Contexto

Um processo operacional sem workflow, checklist ou próxima ação cria estado inconsistente e filas enganosas.

## Decisão

Ao submeter um rascunho, uma única transação deve:

1. validar campos obrigatórios e autorização;
2. selecionar versão do modelo de fluxo;
3. criar as etapas e ativar a primeira;
4. gerar o checklist inicial;
5. definir próxima ação e responsável;
6. alterar o estado para `EM_TRIAGEM`;
7. publicar `FinancingProcessSubmitted` e `WorkflowInitialized`.

Falha em qualquer passo reverte toda a operação. Não haverá `WORKFLOW_PENDING`, `INITIALIZING` ou estado parcialmente inicializado.

Como workflow e documentos são módulos distintos, a submissão será disponibilizada somente quando as capacidades mínimas de ambos existirem. A ordem do MVP prepara workflow na entrega 004 e checklist/documentos na 005; a entrega 005 fecha o caso de uso de submissão.

## Alternativas consideradas

- inicialização eventual: rejeitada sem necessidade de distribuição ou escala.
- estados técnicos intermediários: rejeitados por complexidade artificial.

## Consequências positivas

O processo só entra na fila em estado operacional completo e consistente.

## Consequências negativas

O caso de uso precisa coordenar contratos públicos de três módulos dentro da mesma transação local.

## Riscos

Um adapter que abrir nova transação ou executar efeito externo durante a submissão pode quebrar a atomicidade; testes devem provar rollback integral.

## Evidências e critérios de revisão

Revisar somente se uma dependência externa tornar a transação local inviável.

## Data

2026-08-03
