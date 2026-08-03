# SPEC 008 — Filas e Visões por Perfil

## Objetivo

Entregar consultas operacionais que respondam o que cada usuário precisa fazer ou acompanhar.

## Consultas do analista

- minha fila;
- documentos aguardando análise;
- processos parados;
- pendências vencidas;
- aguardando cliente ou corretor;
- processos sem próxima ação;
- busca e filtros por cliente, corretor, etapa, responsável, situação e prioridade.

## Consultas do corretor

- somente processos vinculados;
- etapa atual;
- última atualização compartilhável;
- pendências atribuídas/compartilháveis;
- próxima ação permitida;
- envio de documento quando autorizado.

## Consultas do cliente

- somente seu processo;
- etapa em linguagem simplificada;
- documentos solicitados e respectivos status;
- pendências próprias;
- histórico compartilhável;
- próxima ação do cliente.

## WhatsApp assistido

Gerar texto e link para compartilhamento manual. Não integrar API oficial nesta fase.

## Regras

- modelos de leitura podem ser específicos;
- consultas não carregam agregados inteiros sem necessidade;
- toda consulta aplica empresa, vínculo e visibilidade;
- paginação é obrigatória em listas internas;
- nenhum indicador representa decisão bancária oficial.

## Testes obrigatórios

- fila correta por responsável;
- filtros combinados;
- corretor sem vínculo;
- cliente acessando outro processo;
- ocultação de notas internas;
- geração de mensagem sem dado excessivo.

## Critérios de aceite

- endpoints entregam dados suficientes para as telas MVP;
- consultas permanecem separadas das regras de escrita;
- desempenho básico é validado com índices adequados.
