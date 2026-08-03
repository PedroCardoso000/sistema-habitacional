# SPEC 004 — Fluxo, Etapas e Próxima Ação

## Objetivo

Preparar a jornada operacional versionada, suas transições e a próxima ação. Rascunhos ainda não recebem workflow; a submissão atômica que inicializa o fluxo será exposta na entrega 005.

## Domínio

Modelar:

- `WorkflowModel` versionado;
- `WorkflowStageDefinition`;
- `ProcessStage`;
- estados de etapa;
- critérios obrigatórios de saída;
- `NextAction` com descrição, responsável e prazo opcional;
- exceção autorizada com justificativa.

## Fluxo inicial do MVP

1. Entrada e análise inicial;
2. Documentação do comprador;
3. Pré-aprovação;
4. Documentação do imóvel;
5. Análise bancária;
6. Contrato e assinatura.

## Regras

- processo em `RASCUNHO` não possui workflow;
- processo submetido/ativo possui etapa atual;
- avanço exige critérios obrigatórios atendidos;
- salto ou retorno exige autorização e justificativa;
- toda mudança preserva etapa anterior;
- processo ativo deve permitir próxima ação e responsável;
- processos sem próxima ação devem ser identificáveis.

## Casos de uso

- configurar/carregar fluxo inicial;
- preparar contrato interno para inicializar fluxo durante a submissão;
- definir ou alterar próxima ação;
- avançar etapa;
- retornar etapa;
- bloquear/desbloquear etapa;
- registrar exceção autorizada;
- consultar jornada do processo.

## Testes obrigatórios

- transição válida;
- transição bloqueada;
- exceção sem justificativa;
- retorno de etapa;
- próxima ação sem responsável;
- histórico da transição;
- tentativa de iniciar workflow em rascunho fora da submissão.

## Critérios de aceite

- detalhe de processo operacional informa etapa atual e próxima ação; detalhe de rascunho informa que ainda não foi submetido;
- regras ficam no domínio e não no controller;
- fluxo inicial é versionado e não codificado de forma espalhada.
