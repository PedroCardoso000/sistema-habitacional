# SPEC 007 — Histórico e Auditoria

## Objetivo

Permitir reconstruir as mudanças relevantes do processo com autoria, data e contexto.

## Domínio e aplicação

Definir eventos internos para, no mínimo:

- processo criado;
- participante associado;
- etapa alterada;
- próxima ação definida;
- documento solicitado, enviado, aprovado ou recusado;
- pendência criada, reatribuída, prorrogada ou resolvida;
- processo suspenso, cancelado, reaberto ou concluído.

## Regras

- eventos históricos são append-only;
- usuário comum não altera nem exclui evento;
- correção produz novo evento;
- evento registra empresa, processo, ator, instante e referência;
- conteúdo compartilhável é separado de nota interna;
- auditoria técnica não substitui histórico de negócio.
- Event Publication Registry não substitui event store ou timeline; a projeção desta entrega é a fonte append-only do histórico de negócio;
- nenhum ambiente com dados relevantes deve operar antes de esta projeção estar instalada.

## Casos de uso

- registrar evento de negócio;
- consultar linha do tempo interna;
- consultar linha do tempo filtrada para corretor;
- consultar linha do tempo simplificada para cliente;
- registrar download e acesso sensível quando exigido.

## Testes obrigatórios

- ordem cronológica;
- imutabilidade;
- filtragem por visibilidade;
- autoria após usuário revogado;
- isolamento entre empresas.

## Critérios de aceite

- cada feature anterior publica seus eventos sem dependência circular;
- linha do tempo pode ser usada pelo frontend;
- histórico não expõe notas internas a usuários externos.
