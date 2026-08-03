# Esteira Habitacional — Documentação de Arquitetura e Boas Práticas

Este diretório registra as decisões iniciais de engenharia para o projeto **Esteira Habitacional**.

A solução é uma central colaborativa de processos de financiamento habitacional. Ela organiza processos, documentos, pendências, responsáveis, etapas e histórico, sem substituir os sistemas oficiais dos bancos.

## Objetivo desta pasta

- orientar a implementação do backend e do frontend;
- preservar o domínio desacoplado de frameworks e infraestrutura;
- manter o código legível, testável e sustentável;
- evitar decisões prematuras de complexidade;
- registrar padrões obrigatórios para evolução do produto.

## Direção arquitetural inicial

- **Backend:** Java, monólito modular, arquitetura hexagonal/clean architecture, domínio rico, casos de uso explícitos e dependências apontando para dentro.
- **Frontend:** aplicação web componentizada, organizada por domínio/feature, com design system e reutilização disciplinada.
- **Integração:** API HTTP síncrona no MVP; eventos internos quando úteis; mensageria e microsserviços apenas diante de necessidade comprovada.
- **Persistência:** abstraída por portas; detalhes de banco e armazenamento ficam na infraestrutura.

## Estrutura

```text
docs/
├── architecture/
│   ├── 01-visao-geral.md
│   ├── 02-modulos-do-dominio.md
│   ├── 03-dependencias-e-limites.md
│   ├── 04-decisoes-iniciais.md
│   └── decisions/
│       ├── README.md
│       └── ADR-001...ADR-012
├── backend/
│   ├── 01-arquitetura-backend.md
│   ├── 02-organizacao-de-pacotes.md
│   ├── 03-boas-praticas-java.md
│   ├── 04-padroes-de-codigo.md
│   └── 05-testes-backend.md
├── frontend/
│   ├── 01-arquitetura-frontend.md
│   ├── 02-componentizacao.md
│   └── 03-estado-dados-e-ui.md
├── quality/
│   ├── 01-seguranca-privacidade.md
│   ├── 02-observabilidade.md
│   └── 03-definition-of-done.md
└── templates/
    ├── adr-template.md
    └── feature-template.md
```

## Regra principal

> O domínio não conhece banco de dados, framework web, ORM, fila, provedor de armazenamento, biblioteca de autenticação ou detalhes de UI.

## Status

Baseline fundacional aprovada em 2026-08-03. As decisões normativas estão no [`docs/architecture/decisions/README.md`](docs/architecture/decisions/README.md). Itens ainda não decididos permanecem explicitamente listados em `04-decisoes-iniciais.md` e não podem ser inventados durante a implementação.

## Skills reutilizáveis

As instruções reutilizáveis para agentes e prompts estão em [`docs/skills`](docs/skills).

A primeira esteira disponível é a de backend:

- [`backend-feature-delivery`](docs/skills/backend/backend-feature-delivery/SKILL.md)
- [`backend-feature-review`](docs/skills/backend/backend-feature-review/SKILL.md)

## Skills de implementação

- Backend: `docs/skills/backend/`
- Frontend: `docs/skills/frontend/`

## Entrega do backend

- [`docs/delivery/backend/README.md`](docs/delivery/backend/README.md): organização dos specs e prompts do MVP e das futuras features.
