# SPEC 006 — Pendências

## Objetivo

Tornar explícita toda ação necessária para destravar ou avançar um processo.

## Domínio

Modelar `PendingAction` com:

- descrição;
- categoria;
- processo e etapa de origem;
- responsável atual;
- prioridade;
- prazo opcional/configurável;
- status;
- histórico de reatribuição e prorrogação.

## Estados

- aberta;
- em andamento;
- aguardando resposta;
- resolvida;
- cancelada.

`Vencida` não é estado persistido. É condição calculada quando o prazo já passou e o status não é resolvido nem cancelado.

## Regras

- toda pendência tem responsável;
- resolução pode exigir validação do analista;
- prorrogação preserva prazo anterior no histórico;
- pendência resolvida não é apagada;
- pendência vencida deve aparecer na fila correta;
- cálculo de vencimento recebe `Clock` ou `Instant` explicitamente;
- participante externo só vê pendências compartilháveis e atribuídas a ele.

## Casos de uso

- criar pendência;
- iniciar atendimento;
- responder pendência;
- reatribuir;
- prorrogar prazo;
- resolver;
- devolver com orientação;
- cancelar;
- identificar vencidas;
- consultar vencidas sem mutar o status persistido.

## Testes obrigatórios

- ausência de responsável;
- resolução por ator sem autorização;
- reatribuição;
- vencimento pelo relógio injetado;
- visibilidade externa;
- isolamento multiempresa.

## Critérios de aceite

- pendências alimentam a fila de trabalho;
- processo mostra pendências abertas e próxima ação;
- relógio é controlável nos testes.
