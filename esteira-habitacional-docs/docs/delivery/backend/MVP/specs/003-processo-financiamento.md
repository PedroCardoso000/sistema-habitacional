# SPEC 003 — Processo de Financiamento

## Objetivo

Criar o agregado central do produto e permitir criar, editar, listar e consultar um rascunho de processo. A entrada operacional será fechada na entrega 005, quando workflow e checklist já existirem.

## Domínio

Agregado `FinancingProcess` com, no mínimo:

- identificador e número do processo;
- empresa;
- origem: corretor ou cliente direto;
- cliente principal;
- participantes vinculados;
- corretor, quando aplicável;
- responsável interno;
- situação geral, inicialmente `RASCUNHO`;
- prioridade;
- imóvel opcional;
- datas relevantes;
- sem workflow ou próxima ação enquanto rascunho;
- versão para concorrência otimista.

## Regras

- rascunho exige empresa, origem e autoria; cliente principal e responsável interno são obrigatórios apenas para submissão;
- processo pode nascer sem imóvel;
- corretor só acessa processo ao qual está vinculado;
- número do processo é único por empresa;
- estado inicial é `RASCUNHO`;
- rascunho não possui workflow, checklist, SLA ou alertas de inatividade;
- rascunho não aparece em filas operacionais nem para cliente ou corretor;
- alterações acontecem por métodos de negócio;
- processo não substitui decisão oficial do banco.

## Casos de uso

- criar rascunho originado por corretor;
- criar rascunho de cliente direto;
- editar dados permitidos do rascunho;
- associar participantes;
- associar ou substituir imóvel preservando histórico;
- alterar prioridade;
- consultar detalhe interno;
- listar processos da empresa com filtros básicos.

## Interfaces externas

- `POST /processes`;
- `GET /processes`;
- `GET /processes/{id}`;
- endpoints específicos para associações quando necessário.

## Testes obrigatórios

- criação válida de rascunho nas duas origens;
- ausência de dados obrigatórios;
- corretor não vinculado;
- processo sem imóvel;
- concorrência otimista;
- isolamento por empresa.

## Critérios de aceite

- rascunho criado aparece somente nas listagens e detalhes internos autorizados;
- domínio não depende da persistência;
- histórico inicial pode ser publicado para a feature 007 consumir.
