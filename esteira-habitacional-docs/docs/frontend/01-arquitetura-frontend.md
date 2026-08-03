# Arquitetura do Frontend

## 1. Objetivo

Construir uma aplicação web responsiva, componentizada e orientada por features, com separação entre UI, regras de apresentação, acesso a dados e estado.

## 2. Estrutura recomendada

```text
src/
├── app/
├── features/
│   ├── financing-process/
│   ├── documents/
│   ├── pending-actions/
│   ├── workflow/
│   ├── brokers/
│   └── clients/
├── components/
│   ├── ui/
│   └── layout/
├── services/
├── hooks/
├── lib/
├── contracts/
└── styles/
```

## 3. Regras

- páginas compõem features;
- componentes de domínio ficam na feature;
- componentes visuais genéricos ficam no design system;
- chamadas HTTP não ficam dentro de componentes visuais;
- regras de autorização visual não substituem autorização backend;
- estado global somente quando realmente compartilhado.

## 4. Fluxo

```text
Page -> Feature Container -> Hook/Use Case de UI -> API Client -> Backend
                     -> Presentational Components
```

## 5. Responsividade

- analista e gestor: desktop-first responsivo;
- corretor e cliente: mobile-first;
- fluxos críticos devem funcionar no navegador móvel;
- tabelas devem possuir alternativas responsivas.

## 6. Base tecnológica e biblioteca de componentes

A escolha inicial para o MVP é:

- Next.js com React e TypeScript;
- Tailwind CSS;
- shadcn/ui como base de componentes;
- Radix UI Primitives para comportamentos acessíveis;
- Lucide React para ícones;
- TanStack Query para estado do servidor;
- React Hook Form e Zod para formulários.

O shadcn/ui foi escolhido por fornecer código editável dentro do projeto, permitindo aderência ao design escuro e específico da Esteira Habitacional sem aprisionar a aplicação a um tema rígido de terceiros.

PrimeReact não será adotado como biblioteca global no MVP. Seu uso futuro deverá responder a uma necessidade concreta, ser avaliado contra componentes existentes e ser registrado por decisão arquitetural.
