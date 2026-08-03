---
name: frontend-feature-delivery
description: Implementa features completas e consistentes no frontend da Esteira Habitacional, priorizando componentização útil, baixo acoplamento, contratos explícitos, manutenção simples, responsividade, acessibilidade e mínimo código necessário.
---

# Skill: Entrega de Feature no Frontend

## 1. Objetivo

Executar uma feature frontend de ponta a ponta sem reescrever instruções arquiteturais em cada prompt.

A implementação deve transformar um requisito de produto em uma experiência completa, integrada e testável, mantendo o código legível, componentizado e simples de manter.

## 2. Fontes obrigatórias

Antes de implementar, leia somente o necessário nestes documentos:

- `docs/frontend/01-arquitetura-frontend.md`
- `docs/frontend/02-componentizacao.md`
- `docs/frontend/03-estado-dados-e-ui.md`
- `docs/quality/01-seguranca-privacidade.md`
- `docs/quality/03-definition-of-done.md`
- documentação funcional da feature solicitada;
- contratos reais da API relacionados à feature;
- componentes e padrões já existentes no repositório.

Não releia o projeto inteiro quando os arquivos diretamente relacionados forem suficientes.

## 3. Stack padrão

Use, por padrão:

- React + TypeScript;
- Next.js;
- Tailwind CSS;
- shadcn/ui;
- Radix UI Primitives;
- Lucide React;
- TanStack Query;
- React Hook Form + Zod.

Não introduza outra biblioteca de componentes, gerenciamento de estado ou formulários sem necessidade comprovada.

## 4. Esteira obrigatória da feature

Toda feature deve seguir esta ordem:

```text
1. Compreender o comportamento e os estados da experiência
2. Confirmar contratos e permissões
3. Definir composição da feature
4. Reutilizar ou criar componentes de UI
5. Implementar integração e estado do servidor
6. Tratar loading, erro, vazio, sucesso e ausência de permissão
7. Validar responsividade e acessibilidade
8. Implementar testes aplicáveis
9. Executar validações do projeto
10. Registrar resultado e limitações
```

A feature só está concluída quando o fluxo principal e seus estados relevantes funcionarem de ponta a ponta.

## 5. Organização recomendada

Organize por feature, evitando pastas genéricas que concentram responsabilidades diferentes.

```text
src/
├── app/
├── features/
│   └── <feature>/
│       ├── api/
│       ├── components/
│       ├── contracts/
│       ├── hooks/
│       ├── lib/
│       ├── schemas/
│       └── tests/
├── components/
│   ├── ui/
│   └── layout/
├── lib/
└── styles/
```

Regras:

- `app/` compõe rotas e layouts;
- `features/` contém comportamento e componentes do domínio da interface;
- `components/ui/` contém primitivas do design system;
- `components/layout/` contém estrutura visual compartilhada;
- chamadas HTTP ficam em `api/`, nunca dentro de componentes puramente visuais;
- schemas de formulários ficam próximos da feature;
- tipos não devem ser duplicados em várias camadas.

## 6. Componentização

### 6.1 Reutilize por conceito

Reutilize componentes quando representarem o mesmo conceito visual ou comportamental.

Não persiga “100% de reutilização”. Isso produz componentes genéricos, props booleanas demais e acoplamento oculto.

### 6.2 Hierarquia

- **Primitivos:** `Button`, `Input`, `Select`, `Badge`, `Dialog`, `Tabs`.
- **Compostos:** `FilterBar`, `StatusBadge`, `EmptyState`, `ConfirmationDialog`.
- **Domínio:** `ProcessQueueItem`, `FinancingJourney`, `DocumentChecklist`, `PendingActionCard`.
- **Página:** compõe features e não concentra regras de negócio da interface.

### 6.3 Critérios para extrair componente

Extraia quando houver pelo menos uma destas condições:

- o mesmo conceito aparece em mais de um lugar;
- a unidade possui comportamento próprio;
- a unidade precisa de teste isolado;
- a leitura da página melhora claramente com a extração.

Não extraia apenas para reduzir artificialmente o tamanho de um arquivo.

## 7. Interfaces e contratos

- Use TypeScript estrito.
- Prefira props pequenas, semânticas e explícitas.
- Não passe respostas brutas gigantes da API para componentes visuais.
- Converta DTOs em modelos de apresentação quando a tela exigir outra forma.
- Centralize endpoints e tipos de API por feature.
- Normalize erros em um ponto conhecido.
- Não replique regras de autorização do backend como se a UI fosse barreira de segurança.
- A UI pode ocultar ações, mas o backend continua sendo a autoridade.

## 8. Estado e dados

### Estado do servidor

Use TanStack Query para:

- consulta;
- cache;
- invalidação;
- mutações;
- revalidação;
- tratamento consistente de loading e erro.

Não copie dados do servidor para estado global sem motivo real.

### Estado local

Use estado local para:

- abertura de modal;
- aba selecionada;
- filtro temporário;
- interação visual isolada.

### Estado global

Só use para informações realmente transversais, como:

- sessão;
- tema;
- preferências globais;
- contexto de empresa atual, quando necessário.

## 9. Design system e biblioteca de componentes

### 9.1 shadcn/ui

- componentes entram no código do projeto;
- adapte-os aos tokens e padrões visuais da Esteira Habitacional;
- não altere primitivas repetidamente em cada feature;
- variantes compartilhadas devem ficar no design system;
- componentes de domínio não devem ser colocados em `components/ui/`.

### 9.2 Tokens visuais

A interface deve usar tokens semânticos, e não cores soltas espalhadas:

- `background` e `surface` em azul-marinho/azul escuro;
- texto principal branco;
- texto secundário em cinza azulado claro;
- azul vivo para ação principal;
- verde para sucesso;
- amarelo ou laranja para atenção;
- vermelho para erro, vencimento ou bloqueio.

Cor nunca deve ser o único indicador de estado.

### 9.3 Iconografia

Use Lucide React como padrão. Não misture bibliotecas de ícones sem justificativa.

## 10. Formulários

Use React Hook Form com Zod quando houver formulário relevante.

Regras:

- schema único para validação da entrada;
- mensagens de erro compreensíveis;
- campos com rótulo associado;
- estado de submissão visível;
- prevenção de submissão duplicada;
- confirmação clara de sucesso ou falha;
- não aceitar silenciosamente valores inválidos.

## 11. UX obrigatória

Toda tela deve tratar, quando aplicável:

- carregamento;
- erro recuperável;
- estado vazio;
- ausência de permissão;
- dados parciais;
- ação em andamento;
- sucesso;
- confirmação antes de ações destrutivas.

Para este produto:

- analista e gestor: experiência desktop-first responsiva;
- corretor e cliente: experiência mobile-first;
- tabelas devem possuir alternativa responsiva;
- a próxima ação deve ser mais visível que informações secundárias;
- linguagem para cliente e corretor deve ser mais simples que linguagem interna.

## 12. Acessibilidade

- navegação por teclado nos fluxos principais;
- foco visível;
- rótulos acessíveis;
- diálogos com foco controlado;
- contraste suficiente;
- estados não representados apenas por cor;
- ícones decorativos sem ruído para leitor de tela;
- mensagens de erro associadas ao campo correspondente.

Prefira os comportamentos acessíveis já fornecidos por Radix/shadcn antes de criar controles interativos do zero.

## 13. Testes

Aplique o nível necessário, sem testar detalhes irrelevantes de implementação.

### Componentes e comportamento

Teste:

- renderização dos estados importantes;
- interação do usuário;
- bloqueio ou exibição conforme permissão;
- submissão e validação de formulários;
- callbacks e efeitos visíveis.

### Integração da feature

Teste:

- consulta bem-sucedida;
- loading;
- erro;
- vazio;
- mutação bem-sucedida;
- erro da mutação;
- atualização ou invalidação da tela.

### Fluxos críticos

Use teste de ponta a ponta para fluxos essenciais, como:

- criar processo;
- enviar documento;
- aprovar ou recusar documento;
- responder pendência;
- alterar etapa;
- consultar processo como corretor ou cliente.

## 14. Regras de código

- nomes devem expressar intenção;
- evite componentes gigantes;
- evite hooks que escondem múltiplas responsabilidades;
- não crie abstrações genéricas antes de existir repetição real;
- não use `any` para escapar de contratos;
- não duplique lógica de transformação;
- não faça requisição diretamente em componente puramente visual;
- não misture regra de apresentação, requisição e layout em um único arquivo;
- remova código morto, logs temporários e mocks não autorizados.

## 15. Proibições

Não fazer sem decisão explícita:

- substituir shadcn/ui por outra biblioteca global;
- introduzir Redux ou outro estado global para dados de servidor;
- criar um design system paralelo dentro de uma feature;
- copiar componentes inteiros para pequenas variações;
- usar PrimeReact e shadcn/ui para o mesmo conjunto de primitivas;
- adicionar dependência apenas para uma função trivial;
- implementar autorização apenas no frontend;
- criar componentes genéricos com dezenas de props booleanas.

## 16. Definition of Done da feature

A feature deve atender, quando aplicável:

- requisito e critérios de aceite implementados;
- contratos alinhados à API real;
- componentes reutilizados ou criados no nível correto;
- estados de UI tratados;
- responsividade validada;
- acessibilidade básica validada;
- testes relevantes passando;
- lint e checagem de tipos passando;
- sem duplicação ou dependência desnecessária introduzida;
- documentação atualizada quando o comportamento público mudar.

## 17. Relatório final esperado

Ao terminar, reporte somente:

1. objetivo entregue;
2. arquivos principais alterados;
3. componentes reutilizados ou criados;
4. integração e contratos usados;
5. testes e comandos executados;
6. limitações ou decisões pendentes.

Não repita toda a skill no relatório.
